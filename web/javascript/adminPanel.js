'use strict';
/*--------------This might be useful for you Ville ---------------*/
const testButton = document.getElementById('testtt');
const infoElement = document.getElementById('adminpanel-wrapper');

testButton.addEventListener('click', () => {
    addUser();
    addListeners();
    console.log("clicked");
});

//Add user function template
const addUser = () => {
console.log("working");
    infoElement.innerHTML += `<div class="user-information-bar">
                                  <div class="info-bar"><img src="resources/default.jpg"></div>
                                  <div class="info-bar-name"><p>Test User</p></div>
                                  <div class="info-bar"><p>754</p></div>
                                  <div class="info-bar"><p>0</p></div>
                                  <div class="info-bar-button"><p>Grant</p></div>
                                  <div class="info-bar-button"><p>Revoke</p></div>
                                  <div class="info-bar-button"><p>Remove</p></div>
                               </div>`;
};

/*---------------------- POP UP ------------------*/

const popUp = document.querySelector(".pop-up");
const popUpButton = document.getElementsByClassName('info-bar-button');
const yesButton = document.querySelector(".yes-button");
const noButton = document.querySelector(".no-button");


const addListeners = () => {    //Function might not be needed once you fetch users from db, wrapped it in function to have it work with test button
for (let i = 0; i < popUpButton.length; i++) {
    popUpButton[i].addEventListener('click', () => {
        document.body.setAttribute("style","pointer-events: none;");
        popUp.style.display = 'flex';
    });
}
};

addListeners();

noButton.addEventListener('click', () => {
    popUp.style.display = 'none';
    document.body.setAttribute("style","pointer-events: auto;");
});

yesButton.addEventListener('click', () => {
    popUp.style.display = 'none';
    document.body.setAttribute("style","pointer-events: auto;");
});