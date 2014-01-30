

class CookbookController {

    def index = {
    	def map = [name:'emiliano', role:'Front-end dev', 
    		books: [['name': '1111', 'author': 'Myself', 'description': 'bla bla bla'],
				['name': '2222', 'author': 'Max', 'description': 'bla bla bla2'],
				['name': '3333', 'author': 'Ann', 'description': 'bla bla bla3']]
    	]
	}
}
