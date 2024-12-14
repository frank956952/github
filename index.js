
const buttons = document.querySelectorAll(".drum");

buttons.forEach(button => {
    
    button.addEventListener("click", () => {
        const buttonInnerHTML = button.innerHTML;
        playSound(buttonInnerHTML); 
        buttonAnimation(buttonInnerHTML); 
    });
});

document.addEventListener("keydown", event => {
    playSound(event.key); 
    buttonAnimation(event.key); 
});


function playSound(key) {
    switch (key) {
        case "w":
            new Audio("sounds/crash.mp3").play();
            break;
        case "a":
            new Audio("sounds/kick-bass.mp3").play();
            break;
        case "s":
            new Audio("sounds/snare.mp3").play();
            break;
        case "d":
            new Audio("sounds/tom-1.mp3").play();
            break;
        case "j":
            new Audio("sounds/tom-2.mp3").play();
            break;
        case "k":
            new Audio("sounds/tom-3.mp3").play();
            break;
        case "l":
            new Audio("sounds/tom-4.mp3").play();
            break;
        default:
            console.log("Invalid key: " + key);
    }
}


function buttonAnimation(currentKey) {
    const activeButton = document.querySelector("." + currentKey);
    if (activeButton) {
        activeButton.classList.add("pressed"); 
        setTimeout(() => {
            activeButton.classList.remove("pressed");
        }, 100); 
    }
}
