 
#!/bin/bash
echo "Recording… Press Ctrl+C to Stop."
arecord -f cd -d 20 -D plughw:1,0 data.wav
# arecord -d 20 -D plughw:1,0 -f cd -t wav data.wav

echo "This is the recording..."
aplay data.wav

echo "convert to flac file format"
avconv -i test.wav -loglevel panic -y -ar 16000 -ac 1 -acodec flac test.flac

echo "voice recording saved as data.flac"

# echo "google speech api call"
# wget -U "Mozilla/5.0" --post-file test.flac --header="Content-Type: audio/x-flac; rate=16000" http://www.google.com/speech-api/v1/recognize?lang=en-us&client=chromium | cut -d" " -f12 > stt.txt



# arecord -D plughw:1,0 -q -f cd -t wav | avconv -loglevel panic -y -i – -ar 16000 -acodec flac file.flac

# wget -q -U “Mozilla/5.0” –post-file file.flac -–header=“Content-Type: audio/x-flac; rate=16000” “http://www.google.com/speech-api/v1/recognize?lang=en-us&client=chromium” | cut -d” -f12 >stt.txt

# echo -n “You Said: ”
# cat stt.txt

# rm file.flac > /dev/null 2>&1

