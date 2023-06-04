Feature: Envío de Ofertas

  Scenario: Enviar una oferta para un proyecto
    Given que el freelancer ha iniciado sesión y se encuentra en la pantalla de un proyecto
    When el freelancer selecciona "Enviar oferta"
    Then se muestra el formulario para enviar una oferta
    When el freelancer ingresa los detalles de la oferta y selecciona "Enviar"
    Then se envía la propuesta al cliente y se muestra un mensaje de "Oferta enviada con éxito"

  Scenario: Intentar enviar una oferta sin completar todos los campos requeridos
    Given que el freelancer ha iniciado sesión y se encuentra en la pantalla de un proyecto
    When el freelancer selecciona "Enviar oferta"
    Then se muestra el formulario para enviar una oferta
    When el freelancer ingresa descripcion pero no presupuesto o viceversa o ninguno y selecciona "Enviar"
    Then se muestra un mensaje de error que indica "Por favor complete todos los campos requeridos"

  Scenario: Intentar enviar una oferta para un proyecto al que ya se ha enviado una oferta
    Given que el freelancer ha iniciado sesión y se encuentra en la pantalla de un proyecto al que ya ha enviado una propuesta
    When el freelancer rellena los campos y selecciona "Enviar oferta"
    Then se envía la propuesta al cliente y se muestra un mensaje de "Oferta enviada con éxito"