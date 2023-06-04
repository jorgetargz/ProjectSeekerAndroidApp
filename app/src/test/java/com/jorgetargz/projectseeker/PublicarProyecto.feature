Feature: Publicación de Proyectos

  Scenario: Crear un proyecto
    Given que el cliente ha iniciado sesión
    When el cliente ingresa los detalles del proyecto y selecciona "Crear"
    Then se vuelve a la lista de proyectos

  Scenario: Intentar crear un proyecto con campos vacíos
    Given que el cliente ha iniciado sesión
    When el cliente deja algunos campos vacíos y selecciona "Crear"
    Then se muestra un mensaje de error que indica "Por favor, complete todos los campos"

  Scenario: Intentar crear un proyecto con fechas en formato incorrecto
    Given que el cliente ha iniciado sesión
    When el cliente ingresa fechas en formato incorrecto y selecciona "Crear"
    Then se muestra un mensaje de error que indica "Fechas invalidas"

  Scenario: Intentar crear un proyecto con fecha de inicio posterior o igual a la fecha límite
    Given que el cliente ha iniciado sesión
    When el cliente ingresa una fecha de inicio posterior o igual a la fecha límite y selecciona "Crear"
    Then se muestra un mensaje de error que indica "Fechas invalidas"

  Scenario: Intentar crear un proyecto con fecha límite anterior a la fecha actual
    Given que el cliente ha iniciado sesión
    When el cliente ingresa una fecha límite anterior a la fecha actual y selecciona "Crear"
    Then se muestra un mensaje de error que indica "Fechas invalidas"

  Scenario: Intentar crear un proyecto con fecha de inicio anterior a la fecha actual
    Given que el cliente ha iniciado sesión
    When el cliente ingresa una fecha de inicio anterior a la fecha actual y selecciona "Crear"
    Then se muestra un mensaje de error que indica "Fechas invalidas"

  Scenario: Intentar crear un proyecto con un presupuesto mínimo mayor al presupuesto máximo
    Given que el cliente ha iniciado sesión
    When el cliente ingresa un presupuesto mínimo mayor al presupuesto máximo y selecciona "Crear"
    Then se muestra un mensaje de error que indica "Rango de presupuesto invalido"

  Scenario: Intentar crear un proyecto con un presupuesto no numérico
    Given que el cliente ha iniciado sesión
    When el cliente ingresa un presupuesto no numérico y selecciona "Crear"
    Then se muestra un mensaje de error que indica "Rango de presupuesto invalido"