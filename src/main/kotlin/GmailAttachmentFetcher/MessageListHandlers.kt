package GmailAttachmentFetcher

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePartBody
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val SENDER_REGEX: Regex = ".*@([^>]*)".toRegex(RegexOption.IGNORE_CASE)

fun List<Message>.printMessageFromMailAddress(): List<Message> {
    this.map { message ->
        message.payload.headers.last { it.name == "From" }.value
    }
        .map {
            SENDER_REGEX.find(it)?.groupValues ?: listOf()
        }
        .distinctBy {
            it.last()
        }
        .forEach {
            println(it.last())
        }

    return this
}

fun List<Message>.downloadAttachments(gmail: Gmail, destination: File): List<Message> {
    println("Downloading attachments")
    if (!destination.exists()) destination.mkdir()

    val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    this.forEach { message ->
        message.payload.parts.filter {
            it.mimeType.contains("pdf")
        }.forEach {
            val attachment: MessagePartBody =
                gmail.Users().Messages().Attachments().get("me", message.id, it.body.attachmentId)
                    .execute()

            val messageInternalDate: LocalDate = Instant.ofEpochMilli(message.internalDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val year: String = messageInternalDate.year.toString()
            val yearDir = File(destination, year)
            if (!yearDir.exists()) yearDir.mkdir()

            val realAttachment: ByteArray = attachment.decodeData()
            val formattedDate: String = messageInternalDate.format(dateFormatter)
            val senderEmail: String = message.payload.headers.last { header -> header.name == "From" }.value
            val company: String = SENDER_REGEX.find(senderEmail)?.groupValues?.last() ?: "UNKNOWN"
            val fileName = "$formattedDate $company - ${it.filename}"

            val targetFile = File(yearDir, fileName)
            targetFile.writeBytes(realAttachment)

            println("Exported $senderEmail to $fileName in folder $yearDir | https://mail.google.com/mail/u/0/#inbox/${message.id}")
        }
    }

    return this
}