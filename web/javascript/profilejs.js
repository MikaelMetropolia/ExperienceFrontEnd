'use strict';



const profileTitleElement = document.querySelector('#profileTitle');
const profileImgElement = document.querySelector('#profileImg');

getUserStats(profileTitleElement, profileImgElement);

const element = document.getElementById('arrow5'),
style = window.getComputedStyle(element),
right5 = style.getPropertyValue('transform');

const element2 = document.getElementById('arrow6'),
style2 = window.getComputedStyle(element2),
right6 = style.getPropertyValue('transform');

const element3 = document.getElementById('arrow7'),
style3 = window.getComputedStyle(element3),
right7 = style.getPropertyValue('transform');

const element4 = document.getElementById('arrow8'),
style4 = window.getComputedStyle(element4),
right8 = style.getPropertyValue('transform');

const dropElement = document.getElementById('dropdown');
const dropElement2 = document.getElementById('dropdown2');
const dropElement3 = document.getElementById('dropdown3');
const dropElement4 = document.getElementById('dropdown4');

const button3 = document.getElementById("uploads");
const button4 = document.getElementById("uploads2");
const button5 = document.getElementById("uploads3");
const button6 = document.getElementById("uploads4");


const popUp = document.querySelector(".pop-up");
const popUpButton = document.getElementsByClassName('not-a-link');
const yesButton = document.querySelector(".yes-button");
const noButton = document.querySelector(".no-button");

for (let i = 0; i < popUpButton.length; i++) {
    popUpButton[i].addEventListener('click', () => {
        document.body.setAttribute("style","pointer-events: none;");
        popUp.style.display = 'flex';
    })
}

noButton.addEventListener('click', () => {
    popUp.style.display = 'none';
    document.body.setAttribute("style","pointer-events: auto;");
});

yesButton.addEventListener('click', () => {
    popUp.style.display = 'none';
    document.body.setAttribute("style","pointer-events: auto;");
});



button3.addEventListener('click', () => {
    hideMenu(right5, element, dropElement);
});
button4.addEventListener('click', () => {
    hideMenu(right6, element2, dropElement2);
});
button5.addEventListener('click', () => {
    hideMenu(right7, element3, dropElement3);
});
button6.addEventListener('click', () => {
    hideMenu(right8, element4, dropElement4);
});

const hideMenu =  (arrow, element, dropElement) => {
        arrow = window.getComputedStyle(element).getPropertyValue('transform');
    if (arrow == 'rotate(-45deg)' || arrow == 'matrix(0.707107, -0.707107, 0.707107, 0.707107, 0, 0)') {
        element.style.transform = "rotate(45deg)";
        arrow = 'rotate (45deg)';
        dropElement.style.display = "inline";
    } else {
        element.style.transform = "rotate(-45deg)";
        dropElement.style.display = "none";
        arrow = 'rotate(-45deg)';
    }
};











