# Modistra - Sistema para la traducción de Modismos

Una aplicación móvil que ayuda a visitantes, migrantes y nuevos residentes a comprender las expresiones culturales locales de Bogotá a través del procesamiento inteligente de audio y texto.

## Funcionalidades Principales

### Captura de Audio Inteligente
- Grabación en tiempo real desde el micrófono del dispositivo
- Soporte para archivos de audio en formato .m4a, .mp3 y .wav
- Conversión automática de voz a texto usando Whisper

### Detección Contextual de Modismos
- Identificación automática de expresiones bogotanas en conversaciones
- Análisis semántico basado en contexto
- Procesamiento de lenguaje natural especializado en cultura local

### Traducción Inteligente
- Explicación clara del significado de cada modismo
- Reemplazo contextual por equivalentes en español neutro
- Interpretación cultural adaptada al contexto de uso

### Diccionario Personal
- Almacenamiento de modismos encontrados
- Historial personalizado de expresiones aprendidas
- Consulta rápida de significados guardados

### Gestión de Usuarios
- Registro y autenticación segura mediante correo electrónico
- Datos personalizados por usuario
- Sincronización de diccionario personal
- Cambio seguro de contraseñas

## Público Objetivo

### Visitantes y Turistas
Comprensión de expresiones locales durante la estancia en Bogotá

### Migrantes y Nuevos Residentes
Aceleración del proceso de integración cultural y social

### Estudiantes y Profesionales
Mejora de la comunicación en entornos educativos y laborales

### Población Local
Exploración de la riqueza lingüística bogotana

## Tecnologías

- **Frontend**: Android (Kotlin + XML)
- **Backend**: API RESTful
- **Reconocimiento de Voz**: Whisper (OpenAI)
- **Procesamiento de Lenguaje Natural**: BETO (BERT en español) entrenado con dataset de modismos bogotanos
- **Traduccion de frase a Lenguaje Natural**: Phi-3-mini-4k-instruct 
- **Base de Datos**: Firebase
- **Arquitectura**: Microservicios con orquestador

## Problemática Abordada

Según la Secretaría Distrital de Planeación (2021), Bogotá alberga aproximadamente **416.145 migrantes**, equivalente al 5.3% de la población total de la ciudad. La mayor concentración se encuentra en las localidades de Suba (16.8%), Kennedy (13.6%) y Usaquén (10%).

La diversidad lingüística enriquece la ciudad, pero también crea barreras de comunicación que afectan:
- Integración social y cultural
- Participación en espacios educativos
- Oportunidades laborales
- Experiencia turística
- Comunicación efectiva en la vida cotidiana

Modistra elimina estas barreras facilitando la comprensión del lenguaje informal bogotano.

## Arquitectura del Sistema

### Flujo de Procesamiento de Audio
```
Audio Input → API Gateway → Orquestador → Whisper (Speech-to-Text) → BETO (Análisis Contextual) → Detección de Modismos → Phi (Traduccion a lenguaje natural)
```

### Flujo de Procesamiento de Texto
```
Audio Input → API Gateway → Orquestador → BETO (Análisis Contextual) → Detección de Modismos → Phi (Traduccion a lenguaje natural)
```

### Motor de Inteligencia Artificial
- **Whisper**: Reconocimiento automático de voz de alta precisión
- **BETO**: Modelo BERT en español fine-tuned con dataset especializado en modismos bogotanos
- Análisis contextual avanzado para interpretación precisa
- **PHI**: Modelo instruccional desarrollado por Microsoft que hace uso de la salida del modelo BETO para traducir la frase con modismos a lenguaje natural

## Requisitos del Sistema

- Sistema Operativo: Android 10 o superior
- Conectividad: Acceso a internet
- Hardware: Micrófono integrado

## Módulos de la aplicacion movil

### Módulo 1: Gestión de Cuentas de Usuario
- Registro de nuevos usuarios
- Autenticación segura
- Gestión de sesiones
- Cambio de credenciales

### Módulo 2: Captura de Entradas
- Entrada de texto mediante teclado
- Grabación de audio en tiempo real
- Procesamiento de archivos de audio

### Módulo 3: Procesamiento de Entradas
- Conversión de audio a texto con Whisper
- Procesamiento directo de entradas textuales
- Normalización de datos de entrada

### Módulo 4: Detección y Traducción de Modismos
- Identificación de modismos usando BETO
- Traducción contextual precisa usando Phi
- Generación de versiones en español neutro

### Módulo 5: Almacenamiento y Consulta
- Gestión de diccionario personal
- Sistema de consulta y búsqueda

## Beneficios del Sistema

- **Comunicación Efectiva**: Facilita la comprensión en conversaciones cotidianas
- **Integración Cultural**: Reduce barreras lingüísticas entre diferentes comunidades
- **Inclusión Social**: Promueve la participación en espacios diversos
- **Preservación Cultural**: Respeta y documenta las particularidades del habla bogotana
- **Experiencia Urbana**: Mejora la calidad de vida en entornos multiculturales


