EJECUTAR EL PROGRAMA
1.Clonar el repositorio
  git clone https://github.com/tuusuario/CamisetasWallapop.git
2.Abrir el proyecto en Android Studio
  Abrir Android Studio
  File → Open…
  Seleccionar la carpeta del proyecto
3.Configurar Firebase
  En Firebase Console → crear un proyecto
  Añadir app Android con tu applicationId
  Descargar google-services.json y colocarlo en app/
  Activar:
    Firebase Authentication
    Firestore
    Storage
5.Ejecutar la aplicación
  Usar un emulador
    Abrir device Manager y añadir un emulador 
    Darle al run del emulador
    Hacer clik en running devices    
  Build clean proyect
  Build assemble proyect
  Pulsar Run ▶️ en Android Studio
CARACTERÍSTICAS PRINCIPALES
Usuarios
  Registro e inicio de sesión con Firebase.
  Perfil editable (nombre + foto subida a Storage).
  Sistema de puntos para comprar productos.
Productos
  Publicación de productos con varias fotos.
  Edición y eliminación de productos propios.
  Listado general con búsqueda y filtrado.
  Detalle con información, galería y vendedor.
Chat en tiempo real
  Mensajes sincronizados mediante Firestore.
  Envío de ofertas desde el chat.
  Aceptar o rechazar ofertas.
Compras
  Compra directa usando puntos.
  Transferencia automática de puntos entre usuarios.
  Pantalla de dirección de envío opcional.
Valoraciones
  El comprador puede valorar al vendedor tras la compra.
  Sistema de media basado en estrellas.
Notificaciones internas
  Alertas visuales para ofertas pendientes.
