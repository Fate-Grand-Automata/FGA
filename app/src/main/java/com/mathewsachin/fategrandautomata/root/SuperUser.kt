package com.mathewsachin.fategrandautomata.root

import java.io.DataOutputStream
import java.lang.Exception

class SuperUser: AutoCloseable {
    var superUser: Process
    var outStream: DataOutputStream

    init {
        try {
            superUser = Runtime.getRuntime().exec("su", null, null)
            outStream = DataOutputStream(superUser.outputStream)

            // We can check if we got root by sending 'id' to the su process which returns current user's id.
            // The response should contain 'uid=0', where 0 is the id of root user.
            writeLine("id")

            // Don't dispose reader or InputStream will be closed
            val reader = superUser.inputStream.bufferedReader()
            val response = reader.readLine()
            if (!response.contains("uid=0")) {
                throw Exception("Not root user")
            }
        }
        catch (e: Exception) {
            throw Exception("Failed to get Root permission", e)
        }
    }

    private fun waitForCommand() {
        // https://stackoverflow.com/a/16160785/5377194
        writeLine("echo -n 0")

        superUser.inputStream.read()
    }

    private fun writeLine(Line: String) {
        outStream.writeBytes("${Line}\n")
        outStream.flush()
    }

    fun sendCommand(Command: String) {
        writeLine(Command)

        waitForCommand()
    }

    override fun close() {
        writeLine("exit")
        superUser.waitFor()
    }
}