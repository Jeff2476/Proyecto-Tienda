/**
 * Agrega un producto al carrito asumiendo cantidad = 1.
 * @param {HTMLFormElement} formulario - El objeto form que contiene el ID del producto.
 */
function addCart(formulario) {
    var idProducto = $(formulario).find('input[name="idProducto"]').val();
    var ruta = $(formulario).attr('action') || '/carrito/agregar';
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: ruta,
        type: 'POST',
        data: {
            idProducto: idProducto
        },
        beforeSend: function (xhr) {
            if (csrfHeader && csrfToken) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        },
        success: function (response) {
            $("#resultBlock").html(response);
            console.log("Producto agregado con cantidad por defecto (1).");
        },
        error: function (xhr, status, error) {
            var mensaje = xhr.responseText || 'Error en la conexión.';
            alert("Error al agregar producto: " + mensaje);
        }
    });
}

function mostrarImagen(input) {
    if (input.files && input.files[0]) {
        const imagen = input.files[0];
        const maximo = 512 * 1024;
        if (imagen.size <= maximo) {
            var lector = new FileReader();
            lector.onload = function (e) {
                $('#blah').attr('src', e.target.result).height(200);
            };
            lector.readAsDataURL(input.files[0]);
        } else {
            alert("La imagen seleccionada es muy grande... no debe superar los 512 Kb!");
        }
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const confirmModal = document.getElementById('confirmModal');
    confirmModal.addEventListener('show.bs.modal', function (event) {
        const button = event.relatedTarget;
        document.getElementById('modalId').value = button.getAttribute('data-bs-id');
        document.getElementById('modalDescripcion').textContent = button.getAttribute('data-bs-descripcion');
    });
});

setTimeout(() => {
    document.querySelectorAll('.toast').forEach(t => t.classList.remove('show'));
}, 4000);
