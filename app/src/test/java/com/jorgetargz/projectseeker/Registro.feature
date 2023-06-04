Feature: Registro de Usuario

  Scenario: Crear una cuenta con correo electrónico, contraseña y teléfono
    Given que el usuario se encuentra en la página de registro
    When el usuario ingresa su correo electrónico, contraseña y número de teléfono y selecciona "Registrarse"
    Then se crea una cuenta para el usuario y se muestra el Home

  Scenario: Intentar crear una cuenta con un correo electrónico ya registrado
    Given que el usuario se encuentra en la página de registro
    When el usuario ingresa un correo electrónico que ya está registrado, una contraseña y un número de teléfono y selecciona "Registrarse"
    Then se muestra un mensaje de error que indica "Correo electrónico ya registrado"

  Scenario: Intentar crear una cuenta con un número de teléfono ya registrado
    Given que el usuario se encuentra en la página de registro
    When el usuario ingresa un correo electrónico, una contraseña y un número de teléfono que ya está registrado y selecciona "Registrarse"
    Then se muestra un mensaje de error que indica "Número de teléfono ya registrado"

  Scenario: Crear una cuenta con la cuenta de Google
    Given que el usuario se encuentra en la página de login
    When el usuario selecciona "Continuar con Google"
    Then se crea una cuenta para el usuario utilizando su cuenta de Google y se muestra el Home