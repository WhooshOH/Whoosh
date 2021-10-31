const myCanvas = new fabric.Canvas("demoCanvas", {
    width: window.innerWidth - 500,
    height: window.innerHeight - 200,
    backgroundColor: "white",
    isDrawingMode: false,
});

$("#draw-btn").on("click", function () {
    setDraw(!myCanvas.isDrawingMode);
});

const setDraw = (active) => {
    if (active) {
        myCanvas.set({ isDrawingMode: true });
        $("#draw-btn").html("Select");
    } else if (!active) {
        myCanvas.set({ isDrawingMode: false });
        $("#draw-btn").html("Draw &#9998;");
    }
};

$("#rect-btn").on("click", function() {
    const rectangle = new fabric.Rect({
        width: 100,
        height: 100,
    });
    myCanvas.add(rectangle);
    myCanvas.setActiveObject(rectangle);
    setDraw(false);
});

$("#text-btn").on("click", function() {
    const textbox = new fabric.Textbox("Click to edit",{
        width : 400,
    });
    myCanvas.add(textbox);
    myCanvas.setActiveObject(textbox);
    setDraw(false);
});

$("#delete-btn").on("click", function () {
    myCanvas.getActiveObjects().forEach((obj) => {
        myCanvas.remove(obj);
    })
    myCanvas.setActiveObject(null);
});
