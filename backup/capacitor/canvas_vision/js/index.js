var app = {
    initialize: function () {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function () {
        let videoStream;
        let video = document.getElementById("video");

        this.receivedEvent('deviceready');

        function success(data) {
            console.log(data);
        }

        function failure() {
            console.log("failure");
        }

        function verifyImage(event) {
                var reader = new FileReader();
                reader.onload = function (event) {
                    let img = new Image();
                    img.onload = function () {
                        let canvas = document.getElementById("imgCanvas");
                        let ctx = canvas.getContext("2d");

                        ctx.clearRect(0, 0, canvas.width, canvas.height);
                        ctx.drawImage(img, 0, 0);

                        let imgData = canvas.toDataURL("image/jpeg", 1);
                        MobileVision.verify(["FACE", imgData], success, failure);
                    }
                    img.src = reader.result;
                }
                if (event.target.files[0]) {
                    reader.readAsDataURL(event.target.files[0]);
                }
        }

	    function showVideo()
	    {
            navigator.mediaDevices.getUserMedia({ audio: false, video: true }).then(
                function(stream)
                    {
                        videoStream = stream;
                        video.srcObject = stream;
                    },errorHandler);
	    }

		function stopVideo()
	    {
            var track = videoStream.getTracks()[0];
            track.stop();
	    }

        errorHandler = function(err) {
            console.log(err);
        }

        document.getElementById("showVideo").addEventListener('click', showVideo);
        document.getElementById("stopVideo").addEventListener('click', stopVideo);

        document.getElementById("checkImage").addEventListener('change', verifyImage);
    },

    // Update DOM on a Received Event
    receivedEvent: function (id) {
        console.log('Received Event: ' + id);
    }
};

app.initialize();