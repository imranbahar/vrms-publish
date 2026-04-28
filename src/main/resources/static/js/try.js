
document.addEventListener("DOMContentLoaded", function(event) {
    const showNavbar = (toggleId, navId, bodyId, headerId) => {
        const toggle = document.getElementById(toggleId),
            nav = document.getElementById(navId),
            bodypd = document.getElementById(bodyId),
            headerpd = document.getElementById(headerId);

        // Validate that all variables exist
        if (toggle && nav && bodypd && headerpd) {
            toggle.addEventListener('click', () => {
                // show navbar
                nav.classList.toggle('show');
                // change icon
                toggle.classList.toggle('bx-x');
                // add padding to body
                bodypd.classList.toggle('body-pd');
                // add padding to header
                headerpd.classList.toggle('body-pd');
            });
        }
    };

    showNavbar('header-toggle', 'nav-bar', 'body-pd', 'header');

    /*===== LINK ACTIVE =====*/
    const linkColor = document.querySelectorAll('.nav_link');

    function colorLink() {
        if (linkColor) {
            linkColor.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        }
    }
    linkColor.forEach(l => l.addEventListener('click', colorLink));
});


// // Format address cells: 4 words per line, max 3 lines
// document.querySelectorAll('.address-cell').forEach(function(cell) {
//     if (cell.innerText) {
//         const words = cell.innerText.split(' ');
//         let formatted = '';
//         let lineCount = 1;
//         for (let i = 0; i < words.length; i++) {
//             formatted += words[i] + ' ';
//             if ((i + 1) % 4 === 0 && i !== words.length - 1 && lineCount < 3) {
//                 formatted += '<br>';
//                 lineCount++;
//             }
//         }
//         cell.innerHTML = formatted.trim();
//     }
// });
