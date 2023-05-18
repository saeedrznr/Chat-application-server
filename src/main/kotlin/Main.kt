import com.google.gson.Gson
import model.SendMessage
import java.net.ServerSocket

val clients = ArrayList<ClientHandler>()
val clientsIP = ArrayList<String>()
fun main() {
        val serverSocket = ServerSocket(9998)
        println("server listening at port ${serverSocket.localPort}:")
        while (true) {
            val socket = serverSocket.accept()
               val client = ClientHandler(socket,onDisconnected)
               println("ip ${socket.inetAddress.hostAddress} connected")
                clients.add(client)
                clientsIP.add(socket.inetAddress.hostAddress)
            println(clientsIP)
            sendUserList()

        }
   




}




val onDisconnected:(ClientHandler)->Unit = {client:ClientHandler ->
    println("ip ${client.socket.inetAddress} disconnected!")
    clients.remove(client)
    clientsIP.remove(client.socket.inetAddress.hostAddress)
    sendUserList()
    println(clientsIP)
}

private fun sendUserList(){
    for (item in clients){
        val ipCopies = clientsIP.toMutableList()
        ipCopies.remove(item.socket.inetAddress.hostAddress)
        val message = SendMessage("server",item.socket.inetAddress.hostAddress,null,0,ipCopies)
        item.sendUsersList(Gson().toJson(message))
    }
}


