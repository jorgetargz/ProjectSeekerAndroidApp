Feature: Inicio de Sesión

  Scenario: Iniciar sesión con correo electrónico y contraseña
    Given que el usuario se encuentra en la página de inicio de sesión
    When el usuario ingresa su correo electrónico y contraseña y selecciona "Iniciar sesión"
    Then se verifica el correo electrónico y la contraseña, se inicia la sesión y se muestra el Home

  Scenario: Intentar iniciar sesión con un correo electrónico incorrecto o no registrado
    Given que el usuario se encuentra en la página de inicio de sesión
    When el usuario ingresa un correo electrónico incorrecto o no registrado y una contraseña, y selecciona "Iniciar sesión"
    Then se muestra un mensaje de error

  Scenario: Intentar iniciar sesión con una contraseña incorrecta
    Given que el usuario se encuentra en la página de inicio de sesión
    When el usuario ingresa su correo electrónico y una contraseña incorrecta, y selecciona "Iniciar sesión"
    Then se muestra un mensaje de error

  Scenario: Iniciar sesión con la cuenta de Google
    Given que el usuario se encuentra en la página de inicio de sesión
    When el usuario selecciona "Continuar con Google"
    Then se inicia la sesión con la cuenta de Google del usuario y se muestra el Home