
const form = document.getElementById("gradeform");
const tableBody = document.querySelector("#scoreTable tbody");
const mathAverageCell = document.getElementById("mathAverage");
const englishAverageCell = document.getElementById("englishAverage");
const overallAverageCell = document.getElementById("overallAverage");


let rowCount = 0;
let totalMath = 0;
let totalEnglish = 0;


form.addEventListener("submit", (event) => {
    event.preventDefault();

    const mathScore = parseFloat(document.getElementById("math").value);
    const englishScore = parseFloat(document.getElementById("english").value);


    const rowAverage = ((mathScore + englishScore) / 2).toFixed(2);


    totalMath += mathScore;
    totalEnglish += englishScore;
    rowCount++;

 
    const newRow = document.createElement("tr");
    newRow.innerHTML = `
        <td>${rowCount}</td>
        <td>${mathScore}</td>
        <td>${englishScore}</td>
        <td>${rowAverage}</td>
    `;
    tableBody.appendChild(newRow);

    updateFooter();


    form.reset();
});


function updateFooter() {
    
    const mathAverage = (totalMath / rowCount).toFixed(2);
    const englishAverage = (totalEnglish / rowCount).toFixed(2);

   
    const overallAverage = ((totalMath + totalEnglish) / (rowCount * 2)).toFixed(2);

   
    mathAverageCell.textContent = mathAverage;
    englishAverageCell.textContent = englishAverage;
    overallAverageCell.textContent = overallAverage;
}
