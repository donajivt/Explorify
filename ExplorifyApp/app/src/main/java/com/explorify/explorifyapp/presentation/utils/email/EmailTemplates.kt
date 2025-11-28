package com.explorify.explorifyapp.presentation.utils.email

fun buildEmailTemplate(
    username: String,
    actionTitle: String,
    actionDescription: String,
    details: String
): String {
    return """
        <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #2e7d32;">Hola $username,</h2>

                <p>$actionDescription</p>

                <h3 style="color: #2e7d32;">$actionTitle</h3>

                <div style="background-color: #f4f4f4; padding: 12px; border-radius: 8px; margin-top: 8px;">
                    <p>$details</p>
                </div>

                <p style="margin-top: 20px;">Si tienes alguna duda, puedes responder a este correo.</p>

                <p style="margin-top: 40px;">Atentamente,<br><b>Equipo de Explorify</b></p>

                <hr>
            </body>
        </html>
    """.trimIndent()
}

fun buildPublicationDeletedTemplate(
    username: String,
    publicationTitle: String,
    reason: String
): String {

    val description = """
        Queremos informarte que una de tus publicaciones fue retirada por incumplir las normas de la comunidad.
    """.trimIndent()

    val details = """
        <b>Título:</b> $publicationTitle<br>
        <b>Motivo:</b> $reason<br>
        <b>Fecha:</b> ${java.time.LocalDate.now()}
    """.trimIndent()

    return buildEmailTemplate(
        username = username,
        actionTitle = "Tu publicación ha sido eliminada",
        actionDescription = description,
        details = details
    )
}
 /*  <small>Este mensaje fue generado automáticamente, por favor no respondas directamente a este correo.</small>*/