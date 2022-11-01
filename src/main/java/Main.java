import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URL;


public class Main {
    private static final String KEY = "bmm59VwgpBg4RLy2k3Bm969y5nLe63PKAvmY3WC8";
    private static final String URL = "https://api.nasa.gov/planetary/apod?api_key=" + KEY;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {

        try (CloseableHttpClient client = clientBuild()) {
            NasaResponse nasaResponse = getNasaResponse(client);
            if (nasaResponse != null) {
                String imageURL = nasaResponse.getUrl();
                String imageHDUrl = nasaResponse.getHdurl();
                String fileName = getImageName(imageURL);
                String fileHDName = getImageName(imageHDUrl);
                if (nasaResponse.getMedia_type().equals("image")) {
                    downloadFile(client, imageURL, fileName);
                    downloadFile(client, imageHDUrl, fileHDName);
                } else {
                    System.out.println("Тип контента не поддерживается");
                }

            } else {
                System.out.println("NASA doesn't response.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void downloadFile(CloseableHttpClient client, String fileUrl, String fileName) throws IOException {
        try (CloseableHttpResponse response = client.execute(new HttpGet(fileUrl));
             OutputStream os = new FileOutputStream(fileName);
             InputStream is = response.getEntity().getContent()) {

            byte[] buffer = new byte[1024];
            int len;
            File image = new File(fileName);
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            System.out.println("Создан файл " + fileName);

        }
    }

    private static void downloadFileViaApach(URL url, String fileName) throws IOException {
        FileUtils.copyURLToFile(url, new File(fileName));
    }


    private static String getImageName(String imageUrl) {
        if (imageUrl != null) {
            String[] parts = imageUrl.split("/");
            return parts[parts.length - 1];
        } else {
            return null;
        }
    }

    private static NasaResponse getNasaResponse(CloseableHttpClient client) {
        try (CloseableHttpResponse response = client.execute(new HttpGet(URL))) {
            NasaResponse nasaResponse = mapper
                    .readValue(response.getEntity().getContent(), new TypeReference<>() {
                    });
            if (nasaResponse != null) {
                return nasaResponse;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static CloseableHttpClient clientBuild() {
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();
    }

}
