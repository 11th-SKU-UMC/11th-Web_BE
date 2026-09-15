const button = document.querySelector("#clapButton");
const clapArea = document.querySelector("#clapArea");
button.addEventListener("click", () => {
  const clap = document.createElement("span");
  clap.textContent = "👏";
  clapArea.appendChild(clap);
});
