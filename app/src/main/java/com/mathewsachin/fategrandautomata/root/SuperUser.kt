package com.mathewsachin.fategrandautomata.root

import java.io.DataOutputStream

/**
 * This class can execute shell commands with superuser rights.
 */
class SuperUser : AutoCloseable {
    var superUser: Process
    var outStream: DataOutputStream

    /**
     * Requests superuser rights and checks if the attempt was successful.
     *
     * @throws Exception if the attempt was unsuccessful
     */
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
        } catch (e: Exception) {
            throw Exception("Failed to get Root permission", e)
        }
    }

    /**
     * Waits until the previous command has finished.
     */
    private fun waitForCommand() {
        // https://stackoverflow.com/a/16160785/5377194
        writeLine("echo -n 0")

        superUser.inputStream.read()
    }

    /**
     * Writes a line to the shell.
     */
    private fun writeLine(Line: String) {
        outStream.writeBytes("${Line}\n")
        outStream.flush()
    }

    /**
     * Executes a shell command and waits until it's finished.
     */
    fun sendCommand(Command: String) {
        writeLine(Command)

        waitForCommand()
    }

    /**
     * Exits the shell, which also terminates the superuser session.
     */
    override fun close() {
        writeLine("exit")
        superUser.waitFor()
    }
}