package model
data class SendMessage(
    val sender:String,
    val receiver:String,
    val type:Int?,// 0 -> text , 1 -> image , 2 -> video , 3 -> voice
    var size:Long,
    val userList:List<String>?
)
