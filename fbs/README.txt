# To convert schema (etl_models.fbs) use this command:

flatc --java -o ../src/main/groovy etl_model.fbs

# You can convert a raw binary file created using the following command:
# flatc --raw-binary -t <path to fbs schema file> -- <path to flatbuffer binary file>

flatc --raw-binary -t etl_model.fbs -- examples/flatbuffers-Co7VCIlQ2ARVBUb4cJoGGrFI6zJPLPMh.txt