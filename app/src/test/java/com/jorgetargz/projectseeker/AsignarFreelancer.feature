Feature: Evaluación de ofertas

  Scenario: Revisar ofertas y seleccionar freelancer
    Given que el cliente ha recibido propuestas para su proyecto
    When el cliente revisa las ofertas y selecciona "Aceptar" en la oferta de un freelancer
    Then la oferta del freelancer cambia a aceptada, el freelancer es seleccionado para el proyecto y el resto de ofertas se marcan como rechazadas

  Scenario: Ver perfil del freelancer durante la revisión de la oferta
    Given que el cliente está revisando las ofertas para su proyecto
    When el cliente selecciona "Ver perfil" en la oferta de un freelancer
    Then se muestra el perfil del freelancer con sus habilidades, experiencia y disponibilidad

  Scenario: Iniciar chat con freelancer durante la revisión de la oferta
    Given que el cliente está revisando las ofertas para su proyecto
    When el cliente selecciona "Chat" en la oferta de un freelancer
    Then se abre la ventana de chat y el cliente puede comenzar a enviar mensajes al freelancer