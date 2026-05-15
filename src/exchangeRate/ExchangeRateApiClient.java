package exchangeRate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;

import exception.ErrorCode;
import exception.SystemException;

// 외부 환율 API 연동
public class ExchangeRateApiClient {

    private static final String API_URL = "https://api.frankfurter.dev/v1/latest?base=USD&symbols=KRW";

    public BigDecimal fetchUsdKrwRate() { // USD/KRW 환율
        HttpURLConnection conn = null;

        try {
            URL url = new URL(API_URL);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            
            // 서버 연결 및 응답 읽기 최대 대기시간 : 5초
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();

            // 응답 코드가 200이 아닐 경우, 즉 성공하지 않았을 경우 즉시 에러 발생
            if (responseCode != 200) {
                throw new SystemException(ErrorCode.EXCHANGE_RATE_API_FAILED);
            }

            StringBuilder response = new StringBuilder();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {

                String line;

                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // KRW 환율만 추출해서 return
            return parseKrwRate(response.toString());

        } catch (Exception e) {
            throw new SystemException(ErrorCode.EXCHANGE_RATE_API_FAILED, e);

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private BigDecimal parseKrwRate(String json) {
        String target = "\"KRW\":";
        int startIndex = json.indexOf(target);

        if (startIndex == -1) { // KRW이 없는 경우
            throw new SystemException(ErrorCode.EXCHANGE_RATE_API_FAILED);
        }

        startIndex += target.length();

        int endIndex = json.indexOf("}", startIndex);

        String rate = json.substring(startIndex, endIndex).trim();

        return new BigDecimal(rate).setScale(4, RoundingMode.HALF_UP); // 데이터 형식 통일 후 return
    }
}