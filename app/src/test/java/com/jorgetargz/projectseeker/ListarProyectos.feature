Feature: Listado de Proyectos

#  Como Cliente

  Scenario: Visualizar todos los proyectos abiertos
    Given que el cliente ha iniciado sesión
    When el cliente selecciona "Ver Proyectos Abiertos"
    Then se muestra la lista de todos los proyectos abiertos

  Scenario: Visualizar todos los proyectos creados por el cliente
    Given que el cliente ha iniciado sesión
    When el cliente selecciona "Ver Mis Proyectos"
    Then se muestra la lista de todos los proyectos creados por el cliente

  Scenario: Visualizar todos los proyectos abiertos creados por el cliente
    Given que el cliente ha iniciado sesión
    When el cliente selecciona "Ver Mis Proyectos Abiertos"
    Then se muestra la lista de todos los proyectos abiertos creados por el cliente

  Scenario: Visualizar todos los proyectos en progreso creados por el cliente
    Given que el cliente ha iniciado sesión
    When el cliente selecciona "Ver Mis Proyectos en Progreso"
    Then se muestra la lista de todos los proyectos en progreso creados por el cliente

#  Como Freelancer

  Scenario: Visualizar todos los proyectos abiertos
    Given que el freelancer ha iniciado sesión
    When el freelancer selecciona "Ver Proyectos Abiertos"
    Then se muestra la lista de todos los proyectos abiertos

  Scenario: Visualizar proyectos abiertos que coincidan con alguna habilidad del freelancer
    Given que el freelancer ha iniciado sesión
    When el freelancer selecciona "Ver Proyectos abiertos con mis habilidades"
    Then se muestra la lista de todos los proyectos abiertos que coincidan con alguna habilidad del freelancer

  Scenario: Visualizar proyectos asignados al freelancer
    Given que el freelancer ha iniciado sesión
    When el freelancer selecciona "Ver Proyectos Asignados"
    Then se muestra la lista de todos los proyectos asignados al freelancer

  Scenario: Visualizar proyectos donde el freelancer ha realizado una oferta
    Given que el freelancer ha iniciado sesión
    When el freelancer selecciona "Ver Proyectos donde he hecho oferta"
    Then se muestra la lista de todos los proyectos donde el freelancer ha realizado una oferta