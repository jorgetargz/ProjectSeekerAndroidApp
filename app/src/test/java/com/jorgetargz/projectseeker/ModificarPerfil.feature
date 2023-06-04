Feature: Actualizaciones de Perfil

  Scenario: Actualizar perfil de freelancer
    Given que el rol activo del usuario es freelancer
    When el freelancer actualiza su perfil con título, descripción, nuevas habilidades y disponibilidad y selecciona "Guardar"
    Then el perfil se actualiza con la nueva información y se muestra un mensaje de "Perfil actualizado con éxito"

  Scenario: Actualizar perfil de cliente
    Given que el rol activo del usuario es cliente
    When el cliente actualiza su perfil con título y descripción y selecciona "Guardar"
    Then el perfil se actualiza con la nueva información y se muestra un mensaje de "Perfil actualizado con éxito"