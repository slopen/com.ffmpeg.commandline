# com.ffmpeg.commandline

usage from javascript:

```
var inputPath = '/path/to/your/input/file';
var outputPath = '/path/to/your/output/file';

var command = [
    '-ss', 1, // trim start [1 sec]
    '-t', 5, // trim length [5 sec]
    '-i', inputPath,
    '-movflags', '+faststart',
    '-preset', 'ultrafast',
    '-strict', '-2',
    '-y',
    '-filter:v', 'crop=640:720:270:0,scale=360:360',
    '-crf', '26',
    '-c:a', 'copy',
    outputPath
];

FFMPEGCommandline.runFFMPEG(
    command,
    function(message){
        // async messages from ffmpeg stdout
        console.log(message);
    },
    function(err){
        // error handler
        console.error(err);
    }
);

```

check this repo:
https://github.com/guardianproject/SSCVideoProto
