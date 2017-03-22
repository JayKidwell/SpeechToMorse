


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;


public class SoundFileToText {
	static Scanner input;
	public static String letter, morseSentence;
//	//
//	public static void main(String[] args) throws Exception {
//		String ogSentence = "";
//		//
//		// debug
//		//
//		System.out.println("call google cloud speech api to convert the flac file to text...");
//	    ogSentence =  googleTranslate("/home/pi/", "brooklyn.flac");
//	    //ogSentence =  googleTranslate("/home/pi/", "test.flac");
//	    //ogSentence =  googleTranslate("C:\\Users\\Don\\Documents\\repos\\SpeechToMorse\\resources\\","test.flac");
//		System.out.println("result=" + ogSentence );
//	}
	  //
	  //
	  //
	public static String googleTranslate(String path, String fileName) throws Exception {
		//
		// copy file to pidecoder google bucket
		//
		Process processUpload;
		System.out.println("start process -- copy file to pidecoder google bucket...");
		processUpload = Runtime.getRuntime().exec("gsutil cp " + path + fileName + " gs://pidecoder/" + fileName);
		//
		// while this is uploading, renew the google credentials and create sync
		// request
		//
		try {
			PrintWriter writer = new PrintWriter("/home/pi/renewAccessToken.sh", "UTF-8");
			writer.println("gcloud auth activate-service-account --key-file=/home/pi/piDecoder-f903aadf5183.json");
			writer.println("gcloud auth print-access-token > /home/pi/accessToken.txt");
			writer.close();
		} catch (IOException e) {
			// do something
		}
		Process processRenew;
		System.out.println("start process -- renewAccessTokens...");
		processRenew = Runtime.getRuntime().exec("bash /home/pi/renewAccessToken.sh");
		//
		// create sync-request file
		System.out.println("create sync-request file...");
		String requestContent = "" + "{" + "'config':{'encoding':'FLAC','sampleRate': 16000,'languageCode': 'en-US'},"
				+ "'audio': {'uri':'gs://pidecoder/" + fileName + "'}" + "}";
		try {
			PrintWriter writer = new PrintWriter("/home/pi/sync-request.json", "UTF-8");
			writer.print(requestContent);
			writer.close();
		} catch (IOException e) {
			// do something
		}
		//
		// wait for everything to catch up
		//
		System.out.println("wait for upload process...");
		processUpload.waitFor();
		processUpload.destroy();
		//
		System.out.println("wait for renew process...");
		processRenew.waitFor();
		processRenew.destroy();
		//
		// read in the access token and create the curl request
		//
		System.out.println("read in the access token and create the curl request...");
		String accessToken="";
		Path accessTokenPath = Paths.get("/home/pi", "accessToken.txt");
	    Charset charset = Charset.forName("ISO-8859-1");
	    try {
	      List<String> lines = Files.readAllLines(accessTokenPath, charset);
	      if(lines.size()>0){
	    	  accessToken=lines.get(0);
	      }
	    } catch (IOException e) {
	      System.out.println(e);
	    }
		//
		// execute translation request
		//
		System.out.println("read in the access token and create the curl request...");
		String curlCmd = "curl -s -k"
				+ " -o /home/pi/sync-response.json"
				+ " -H \"Content-Type: application/json\""
				+ " -H \"Authorization: Bearer " + accessToken + "\""
				+ " https://speech.googleapis.com/v1beta1/speech:syncrecognize -d @/home/pi/sync-request.json";
		try {
			PrintWriter writer = new PrintWriter("/home/pi/curlRequest.sh", "UTF-8");
			writer.print(curlCmd);
			writer.close();
		} catch (IOException e) {
			// do something
		}
		//
		Process processRequest;
		System.out.println("start process -- renewAccessTokens...");
		processRequest = Runtime.getRuntime().exec("bash /home/pi/curlRequest.sh");
		processRequest.waitFor();
		processRequest.destroy();
		//
		// return the first transcript found, or return "I didnt understand"
		//
		try {
			String jsonContent = new String(Files.readAllBytes(Paths.get("/home/pi/sync-response.json")));
			ResponseData response = new Gson().fromJson(jsonContent, ResponseData.class);
			for (ResultData result : response.results) {
				for (AlternativeResultsData alternative : result.alternatives) {
					return alternative.transcript;						
				}
			}
		} catch (Exception ex) {
			//			
		} 
		return "I didn't understand";
	}
}

class AlternativeResultsData {
	public String transcript;
	public String confidence;
}

class ResultData {
	public List<AlternativeResultsData> alternatives;
}

class ResponseData {
    public List<ResultData> results;
}


