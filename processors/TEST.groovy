import com.ideas.anymocker.core.components.GroovyHelper

def init(log,requests){

    }
    def process(log,match,body,req){
        log.info("Processing")
        return GroovyHelper.getResponseEntity("success",200)
    }
    def post(log,match,body,req){
        log.info("Post")
    }

    def pre(log,match,body,req){
        log.info("Pree")
    }
    def download(log,match){

    }