# Magnolia event-driven

Implementación del patrón de diseño event-driven para exponer contenidos a servicios de forma asíncrona 

## Features
Publica/produce los contenidos de magnolia en colas kafka en función de la configuración de endpoints del módulo REST Content Delivery

Integración con Apache Kafka. Permite consumidores simultáneos.  
  

**Ejemplo**
>Los nodos de un workspace + nodetype sólo serán publicados si existe un endpoint para dicho workspace + nodetype configurado en REST Content Delivery


## Usage
1. Instalar Zookeeper y Apache Kafka. https://kafka.apache.org/quickstart  

2. Configurar host de Apache Kafka en magnolia-event-driven-service/config@mq_server. El valor para instalación estándar de Kafka sería localhost:9092 

#### Comandos  

##### asyncdelivery-deliver  
Objetivo: publicación de nodo en Kafka (la publicación depende de los endpoint configurados eb REST content delivery). El comando puede ser incluido en el command group de publicación.
  

Parámetros:  
> repository: nombre del workspace del nodo a publicar  

> path: path del nodo a publicar  

Ejemplo:  
~~~~
cm = info.magnolia.commands.CommandsManager.getInstance()
command = cm.getCommand('asyncdelivery', 'deliver')
command.setRepository('tours')
command.setPath('/magnolia-travels/Vietnam--Tradition-and-Today')
command.execute(ctx)
~~~~

**NOTA**
> El nombre del topic de publicación corresponde al path del endpoint reemplazando "/" por "_"

## Contribute to the Magnolia component ecosystem
It's easy to create components for Magnolia and share them on github and npm. I invite you to do so and join the community. Let's stop wasting time by developing the same thing again and again, rather let's help each other out by sharing our work and create a rich library of components.

Just add `magnolia-light-module` as a keyword to npm's package.json to make them easy to find and use on npm.

## License

MIT

## Contributors

Formentor Studio, http://formentor-studio.com/

Joaquín Alfaro, @Joaquin_Alfaro