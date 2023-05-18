import com.google.gson.Gson
import kotlinx.coroutines.*
import model.ReceiveMessage
import model.SendMessage
import java.net.Socket
import java.time.LocalDateTime

class ClientHandler(
    val socket: Socket,
    val onDisconnect: (ClientHandler) -> Unit
)  {
    val BUFFER_SIZE = 16*1024
    val inputStream = socket.getInputStream()
    val outputStream = socket.getOutputStream()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (true){
                try {
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytes = inputStream.read(buffer)
                    val bufstr = String(buffer.copyOfRange(0,bytes))
                    if (bufstr.startsWith("-->")) {
                        val rmessage = Gson().fromJson<ReceiveMessage>(
                            bufstr.substring(3, bufstr.length - 3),
                            ReceiveMessage::class.java
                        )


                        val receiverClient = getClient(rmessage.receiver) ?: continue
                        val smessage = SendMessage(socket.inetAddress.hostAddress,receiverClient.socket.inetAddress.hostAddress,rmessage.type,rmessage.size,null)
                         receiverClient.sendMessage("-->${Gson().toJson(smessage)}<--".toByteArray())

                        var size = rmessage.size
                        if (rmessage.type==0){

                            bytes = inputStream.read(buffer, 0, Math.min(buffer.size.toLong(), size).toInt())
                            while (size > 0 && bytes != -1) {
                                 receiverClient.sendMessage(buffer.copyOfRange(0, bytes))
                                size -= bytes
                                bytes = inputStream.read(buffer, 0, Math.min(buffer.size.toLong(), size).toInt())
                            }
                        }else{

                            bytes = inputStream.read(buffer, 0, Math.min(buffer.size.toLong(), size).toInt())
                            while (size > 0 && bytes != -1) {
                                receiverClient.sendMessage(buffer.copyOfRange(0, bytes))
                                size -= bytes
                                bytes = inputStream.read(buffer, 0, Math.min(buffer.size.toLong(), size).toInt())
                            }

                        }

                    }
                }catch (e:Exception){
                    socket.close()
                    onDisconnect(this@ClientHandler)
                    break
                }

            }
        }
    }


    private fun getClient(ip:String):ClientHandler?{
        for (item in clients){
            if (item.socket.inetAddress.hostAddress==ip)
                return item
        }
        return null
    }

    private fun sendMessage(bytes: ByteArray){
            outputStream.write(bytes)
            outputStream.flush()
    }


    fun sendUsersList(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val m = "-->$message<--".toByteArray()
            outputStream.write(m)
            outputStream.flush()
        }
    }


}