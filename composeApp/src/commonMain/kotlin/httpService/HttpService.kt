package httpService

class HttpService {

//    suspend inline fun <reified T, reified U> postRequest(url:String, requestDto: T): U  {
//        val client = HttpClient(CIO)
//
//        // Codificar a través del modelo a Json en formato String
//        var body: String =  Json.encodeToString(requestDto)
//        println("REQUEST BODY: \n ${Json.encodeToString(body)}")
//
//        // Hacemos el proceso inverso, sólo para si queremos depurar ver que
//        // hemos decodificado al modelo correctametne
//        var loginModel = Json.decodeFromString<T>(body);
//        print(loginModel)
//
//        // Realizamos la petición al servidor
//        val response: HttpResponse = client.post(url) {
//            contentType(ContentType.Application.Json)  // Establecemos el tipo de contenido
//            setBody(body)
//        }
//
//        // Verificamos la respuesta
//        if(response.status == HttpStatusCode.OK){
//            println("RESPONSE OK: \n ${response.bodyAsText()}")
//            return Json.decodeFromString<U>(response.bodyAsText());
//        }
//        println("ERROR PERFORMING REQUEST [${response.status}]: \n ${response.bodyAsText()}")
//        val strErrorResponse: String = "{ \"error\": \"${response.status}\"}";
//        return Json.decodeFromString<U>(strErrorResponse);
//    }
}