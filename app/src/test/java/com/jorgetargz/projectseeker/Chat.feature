Feature: Chat entre Usuarios

  Scenario: Acceder a los chats desde la pantalla de chats
    Given que el usuario ha iniciado sesión y se encuentra en la pantalla de chats
    When el usuario selecciona un chat existente
    Then se abre la ventana de chat y el usuario puede continuar la conversación

  Scenario: Enviar un mensaje en el chat
    Given que el usuario ha iniciado sesión y se encuentra en un chat abierto
    When el usuario ingresa un mensaje en el campo de texto y selecciona "Enviar"
    Then el mensaje se envía y se muestra en la conversación

  Scenario: Recibir un mensaje en el chat
    Given que el usuario ha iniciado sesión y se encuentra en un chat abierto
    When el otro usuario envía un mensaje
    Then el mensaje se recibe y se muestra en la conversación

  Scenario: Volver a la lista de chats desde un chat abierto
    Given que el usuario ha iniciado sesión y se encuentra en un chat abierto
    When el usuario selecciona "Volver"
    Then se cierra el chat y se vuelve a la pantalla de chats

  Scenario: Crear un nuevo chat desde la pantalla de chats
    Given que el usuario ha iniciado sesión y se encuentra en la pantalla de chats
    When el usuario selecciona "Nuevo chat" y selecciona a un usuario de la lista
    Then se abre un nuevo chat con ese usuario