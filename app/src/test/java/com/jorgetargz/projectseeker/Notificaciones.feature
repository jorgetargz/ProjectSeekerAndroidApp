Feature: Notificaciones de Actualizaciones y Mensajes

  Scenario: Recibir notificaciones de actualizaciones de ofertas
    Given que el freelancer ha enviado ofertas para proyectos
    When hay una actualización en una oferta
    Then el freelancer recibe una notificación con los detalles de la actualización de la oferta

  Scenario: Recibir notificaciones de actualizaciones de proyectos
    Given que el cliente ha publicado proyectos
    When hay una actualización en uno de sus proyectos, como la recepción de una nueva oferta
    Then el cliente recibe una notificación con los detalles de la actualización del proyecto

  Scenario: Recibir notificaciones de nuevos mensajes
    Given que el usuario está participando en conversaciones de chat
    When hay un nuevo mensaje en una de las conversaciones
    Then el usuario recibe una notificación con un adelanto del nuevo mensaje