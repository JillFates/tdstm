#!/usr/bin/env node
/**
 * nextman.js - TransitionManager tool built on top of newman API to  run postman collection 
 * on command-line interface environment.
 * 
 * The current features are supported by nextman.js:
 *    - run one or more postman collection version 1.0.0, 2.0.0, 2.1.0 (see run command)
 *    - run all the founded collection (*.json) in one or more directories (see rundir command)
 *    - combine one or more collection of the same schema. (see --combine option)
 *    - filter collection request attributes and values. (see --filter option)
 * 
 * INSTALLATION
 *  nextman.js was built on top of newman, commander and deepmerge nodejs libraries, therefore
 *  these libraries must be installed in order to have the nextman.js working properly.
 *      $ npm install
 * 
 * EXAMPLE OF USAGE:
 *  the following commands are currently supported by nextman.js
 *     $ ./nextman --help
 *     $ nextman run <collection1> [collection2.json, ...] - Run one or more collection.
 *     $ nextman rundir <dir1> [dir2, ...] - run a entire directory of collections.
 * 
 * Copyright (C) 2009-2018 Transitional Data Services, Inc. (TDS), All Rights Reserved
 */
const fs     = require('fs');
const path   = require('path');
const newman = require('newman');
const program = require('commander');
const deepmerge = require('deepmerge');

const PROGRAM_VERSION = '1.0.0';
const PROGRAM_NAME    = 'nextman';

/**
 * Collection Schema 
 * 
 * Given a collection (JSON Object) return the  collection schema version (1.x, 2.x) 
 * based on the collection keys founded. 
 * This function will return 'undefined' when the collection could not be identified.
 * 
 * @param {JSON Object} collection - A collection object on JSON representation.
 */
const collection_schema = (collection) => {
    const collection_schema_1x = ['id', 'name', 'order', 'requests'];
    const collection_schema_2x = ['info', 'item'];

    let collection_keys = Object.keys(collection);

    if (collection_keys.filter(k => collection_schema_1x.includes(k)).length === collection_schema_1x.length) {
        return '1x'
    }
    else if (collection_keys.filter(k => collection_schema_2x.includes(k)).length === collection_schema_2x.length) {
        return '2x'
    }

    return undefined;
}

/**
 * Read Collection
 * 
 * Given a local filesystem path, read the file content and convert into a collection (JSON Object).
 * When specified, the filters could be used to filter any collection requests. The filter must be array
 * of arrays where the inner array contains the [key, value] searched inside the collection request.
 * 
 * Example: filepath = collection1.json, filters = [ ['name', 'Login'], ['method', 'Post'] ]
 * 
 * @param {*} filepath - The filesystem path to the collection file.
 * @param {*} filters  - Array of key, values used to filter the collection request.
 */
const read_collection = (filepath, filters = []) => {
    if (! fs.existsSync(filepath)) {
        throw `Could not located the filepath: ${filepath}`;
    }

    let collection = JSON.parse(fs.readFileSync(filepath));
    let schema = collection_schema(collection);

    if (schema === undefined) {
        console.warn(`WARN: skipping collection ${filepath} due to unsupported schema`);
    }
    else collection['nextman_schema'] = schema;

    // filter collection request, based on cli filter
    if (filters.length > 0) {
        const should_match = (program.and) ? filters.length : 1;
        const should_match_all = (program.shouldmatch) ? filters.length : 1;
        console.log('Should match all the work: ', should_match_all);

        if (collection.nextman_schema === '1x') {
            collection.requests = collection.requests
                .filter(r => filters.filter(f => Object.keys(r).includes(f[0]) && r[f[0]] === f[1]).length >= should_match)

            if (collection.requests.length <= 0) {
                console.error(`Could not found any request using the filters. ${filters}`);
            }
        }
        else {
            collection.item = collection.item
                .filter(i => filters.filter(f => Object.keys(i).includes(f[0]) && i[f[0]] === f[1]).length >= should_match)

            if (collection.item.length <= 0) {
                console.error(`Could not found any item using the filters. ${filters}`);
            }
        }
    }

    console.info(`INFO: Loading the collection from ${filepath}`);
    return collection;
}

/**
 * Combine Collections
 * 
 * Given two collection of the same schema version, combine the two using 
 * the deepmerge library. A error will be issue if the collection has
 * different schema versions. 
 * 
 * @param {Array of Objects} collections - Array of collections (JSON Object)
 */
const combine_collections = (collections) => {
    let combined_collection = {}, collections_names = [];

    collections.forEach(collection => {
        if (combined_collection.nextman_schema && 
            combined_collection.nextman_schema !== collection.nextman_schema) {

            let cl1 = (combined_collection.nextman_schema === '1x')
                ? `${combined_collection.name}::${combined_collection.nextman_schema}`
                : `${combined_collection.info.name}::${combined_collection.nextman_schema}`;

            let cl2 = (collection.nextman_schema === '1x')
                  ? `${collection.name}::${collection.nextman_schema}`
                  : `${collection.info.name}::${collection.nextman_schema}`;

            throw `Cannot combine collection of different schema: '${cl1}' -> '${cl2}' when merging`;
        }

        if (collection.nextman_schema === '1x') {
            collections_names.push(collection.name);
        }
        else {
            collections_names.push(collection.info.name);
        }

        // deep merge the collections.
        combined_collection = deepmerge(combined_collection, collection);
    });

    combined_collection.name = `Combined collection from: ${collections_names.join(', ')}`;
    return combined_collection;
}

/**
 * Run a Collection
 * 
 * Given a collection, the program options trigger newman to run the filtered and/or
 * combined collection. A on_start and on_done function could be specified as callback
 * when the collection start running and after the collection is executed. 
 * 
 * @param {*} collection - Postman collection (JSON Object)
 * @param {*} program    - The program (commander) object with the command-line options.
 * @param {*} on_start   - Callback function to be executed prior to collection execution.
 * @param {*} on_done    - Callback function to be executed after the collection execution.
 */
const run_collection = (collection, program, on_start = undefined, on_done = undefined) => {

    // running collection name.
    if (collection.nextman_schema === '1x') {
        console.info(`Running collection: '${collection.name}' : Description '${collection.description}'`);
    }
    else if (collection.nextman_schema === '2x') {
        console.info(`Running collection: '${collection.info.name}' : Description '${collection.info.description}'`);
    }

    // fire up newman...
    newman.run({
            collection: collection,
            reporters: ['cli'].concat(program.reporters.split(',').map(e => e.trim())),
            reporter: {
                html: {
                    export: './nextman_report.html',
                    template: './templates/htmlreqres.hbs',
                }
            },
            environment: program.environment,
            globals: program.global,
            interationCount: program.interation_count,
            interationData: program.interation_data,
            timeout: program.timeout,
            timeoutRequest: program.timeout_request,
            timeoutScript: program.timeoutScript,
            delayRequest: program.delay_request,
            ignoreRedirects: program.ignore_redirects,
            insecure: program.insecure,
            color: program.color,
            noColor: program.noColor,
            sslClientCert: program.sslClientCert, 
            sslClientKey: program.ssl_client_key,
            sslClientPassphrase: program.ssl_client_passphrase,
        })
        .on('start', 
            (error, args) => {
                if (on_start !== undefined && on_start instanceof Function) on_start(error, args);
            }
        )
        .on('done',
            (error, summary) => {
                if (on_done !== undefined && on_end instanceof Function) on_done(error, summary);

                if (error || summary.error) {
                    console.error('collection run encountered an error.');
                }
                else {
                    // console.log('collection run completed.', summary);
                }
            }
        );
}

/**
 * Run a directory of collections.
 * 
 * Given the directory path, find all the collection (*.json) file and execute
 * on the founded order (Alphabetical order). 
 * 
 * @param {*} dir_path - Path to collections directory.
 * @param {*} program  - Program (commander) command-line options.
 * @param {*} combine  - Whether to combine all the collections or not.
 * @param {*} filters  - Array of key and values to filters on the collection requests.
 */
const run_directory = (dir_path, program, combine = false, filters = []) => {
    if (dir_path === undefined) {
        throw('A directory must be specified');
    }

    if (! fs.existsSync(dir_path)) {
        throw(`Could not locate the directory, ${directory}`);
    }

    let directory_stat = fs.statSync(dir_path);
    if (directory_stat.isDirectory()) {
        let collections = [];

        // Read each file on directory.
        fs.readdirSync(dir_path)
            .map(file => path.join(dir_path, file))
            .forEach(file => {
                let collection = read_collection(file, filters);
                collections.push(collection);
            }
        );

        // When combine, it will combined the collections into one single
        // collection, otherwise it run one founded collection at time.
        if (combine) {
            run_collection(combine_collections(collections), program);
        }
        else collections.forEach(collection => run_collection(collection, program));
    }
}

/*
 * --------------------------------------------------------------------
 *  MAIN SCRIPT STARTS HERE.
 * --------------------------------------------------------------------
 */

// Define the nextman program line options.
program.version(PROGRAM_VERSION)
    .usage('[COMMAND] [options] TARGET1 TARGET2 ...')
    .option('-e, --environment [path]', 'Specify a URL or Path to a Postman Environment')
    .option('-g, --globals [path]', 'Specify a URL or Path to a file containing Postman Globals.')
    .option('-r, --reporters [reporters]', 'Specify the reporters to use for this run. (default: cli)')
    .option('-n, --interation_count [n]', 'Define the number of iterations to run.')
    .option('-d, --interation_data [path]', 'Specify a data file to use for iterations (either json or csv).')
    .option('-D, --delay_request [path]', 'Specify the extent of delay between requests (milliseconds) (default: 0)')
    .option('-C, --color', 'Force colored output (for use in CI environments).')
    .option('-c, --no-color', 'Disable colored output (for use in CI environments).')
    .option('-t, --timeout [n]', 'Specify a timeout for collection run (in milliseconds) (default: 0)')
    .option('-k, --insecure', 'Disable SSL validations')
    .option('-f, --filter [key1=valu1, key2=value2]', 'Filter the collections requests based on key=value object.')
    .option('-m, --shouldmatch', 'When set, the filter specified values will should match all the specified keys and values. ')
    .option('--combine', 'When set to true, will combine the collection into one single collection.')
    .option('--timeout_request', 'Specify a timeout for requests (in milliseconds). (default: 0)')
    .option('--timeout_script [n]', 'Specify a timeout for script (in milliseconds). (default: 0)')
    .option('--ignore_redirects [n]', 'If present, Newman will not follow HTTP Redirects.')
    .option('--ssl_client_cert [path]', 'Specify the path to the Client SSL certificate. Supports .cert and .pfx files.')
    .option('--ssl_client_key [path]', 'Specify the path to the Client SSL key (not needed for .pfx files)')
    .option('--ssl_client_passphrase [path]', 'Specify the Client SSL passphrase (optional, needed for passphrase protected keys).');

/**
 * run collection.
 */
program.command('run [targets...]')
    .description('Run one or more collections')
    .action((targets, options) => {

        // define default values.
        const combine = program.combine || false;

        // get the defined filters
        let filters = [];
        if (program.filter !== undefined) {
            filters = program.filter.split(',').map(values => values.trim().split('='));
        }

        // Run collections
        if (targets && targets.length >= 1) {
            let collections = [];

            // run all system collections.
            if (! combine) {
                targets.map(target => read_collection(target, filters))
                    .forEach(collection => run_collection(collection, program));
            }
            else {
                let combined_collections =
                    combine_collections(targets.map(target => read_collection(target, filters)));

                run_collection(combined_collections, program);
            }
        }
        else throw 'You must specify at least one collection to be executed';
    });

/**
 * rundir command
 */
program.command('rundir [dirs...]')
    .description('Run a entire directory with collections: dir/*.json')
    .action((dirs, options) => {

        // define default variables
        const combine = program.combine || false;

        // get the defined filters
        let filters = [];
        if (program.filter !== undefined) {
            filters = program.filter.split(',').map(values => values.trim().split('='));
        }
 
        // Run specified collections.
        if (dirs && dirs.length >= 1) {
            dirs.forEach(dir => run_directory(dir, program, combine, filters));
        }
        else throw 'You must specify at least one collections directory';
    });

/**
 * Display examples in conjunction with the help options.
 */
program.on('--help', () => {
    console.info('');
    console.info('  Examples: ');
    console.info('');
    console.info('    Run two collections')
    console.info(`    \$ ${PROGRAM_NAME} run -e env.json --insecure collection1.json collection2.json\n`)

    console.info('    Combine two collection into one. (--combine).');
    console.info(`    \$ ${PROGRAM_NAME} run -e env.json --insecure collection1.json collection2.json --combine\n`);

    console.info('    Run entire directory of collections (rundir)');
    console.info(`    \$ ${PROGRAM_NAME} rundir collection_dirs -e env.json --insecure\n`);
    console.info('');

    console.info('    Filter by collections name');
    console.info(`    \$ ${PROGRAM_NAME} run collection_dirs -e env.json --insecure --filter 'name=Login, name=Create User'\n`);
})

/**
 * Unsupported command.
 */
program.command('*')
    .description("\r ") // workaround to remove the * from the screen.
    .action((command) => {
        console.info(`The command '${command}' is not a valid command, check the available commands below`);
        program.help();

        process.exit(2);
    })

// far we go
try {
    if (process.argv.length <= 2) {
        console.info('You must specify one of the availables commands');
        program.help();
    }

    program.parse(process.argv);
}
catch (err) {
    console.error('[ERROR]: Nextman failed with the following error message: ');
    console.error(`    ${err}`);
    console.error(`Please run '${PROGRAM_NAME} --help' for usage details`);
    process.exit(1);
}
