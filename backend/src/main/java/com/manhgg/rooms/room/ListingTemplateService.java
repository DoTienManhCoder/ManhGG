package com.manhgg.rooms.room;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListingTemplateService {
  private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
  private static final String DEFAULT_CONTACT = "Mạnh - SĐT/Zalo/Mess 0353830297";
  private static final String STYLE_SAMPLE = """
      CHO THUÊ CĂN HỘ EMERALD - CELADON CITY
      Căn hộ gọn đẹp, đầy đủ nội thất - dọn vào ở ngay
      Diện tích: 63m2
      Thiết kế: 2 phòng ngủ | 1 WC
      Full nội thất đẹp, bố trí gọn gàng - tiện nghi
      Giá tốt, hỗ trợ kéo giá cho khách thiện chí
      Liên hệ: Mạnh - SĐT/Zalo/Mess 0353830297 để xem phòng thực tế
      """;

  private final RestClient restClient;
  private final String apiKey;
  private final String model;

  public ListingTemplateService(
      RestClient.Builder restClientBuilder,
      @Value("${app.gemini.api-key:}") String apiKey,
      @Value("${app.gemini.model:gemini-2.5-flash}") String model) {
    this.restClient = restClientBuilder.build();
    this.apiKey = apiKey;
    this.model = model;
  }

  public String generate(Room room) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình Gemini API key");
    }

    try {
      JsonNode response = restClient.post()
          .uri(GEMINI_ENDPOINT, model)
          .header("x-goog-api-key", apiKey)
          .body(requestBody(room))
          .retrieve()
          .body(JsonNode.class);

      String text = extractText(response);
      if (text.isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini không trả về nội dung mẫu tin");
      }
      return scrubInternalDetails(text, room).trim();
    } catch (RestClientResponseException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể tạo mẫu tin từ Gemini", exception);
    }
  }

  private Map<String, Object> requestBody(Room room) {
    return Map.of(
        "systemInstruction", Map.of(
            "parts", List.of(Map.of("text", systemInstruction()))),
        "contents", List.of(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", userPrompt(room))))),
        "generationConfig", Map.of(
            "temperature", 1.05,
            "topP", 0.95,
            "maxOutputTokens", 1200,
            "thinkingConfig", Map.of("thinkingBudget", 0)));
  }

  private String systemInstruction() {
    return """
        Bạn là chuyên gia viết tin đăng cho sale phòng trọ, căn hộ cho thuê tại Việt Nam.
        Hãy viết tiếng Việt có dấu thật tự nhiên, hấp dẫn, dùng để đăng Facebook/Zalo.
        Chỉ nói những điểm hay, thông tin có lợi cho khách và địa chỉ ẩn/khu vực được phép công khai.
        Tuyệt đối không đưa địa chỉ đúng nội bộ, thông tin chủ nhà, điểm không tốt, phí/lưu ý nội bộ vào tin đăng.
        Nếu note nội bộ có thông tin mâu thuẫn với điểm hay, hãy tránh nói quá đà và không bịa đặt.
        Không tự bịa tiện ích, diện tích, nội thất hoặc thông tin liên hệ nếu dữ liệu không có.
        Tuyệt đối không ghi giá thuê chính xác trong tin đăng, dù dữ liệu có giá. Chỉ được nói mềm như "giá tốt", "có thể kéo giá", "inbox để nhận giá tốt".
        Bắt buộc làm rõ các thông tin quan trọng bằng từng dòng riêng: địa chỉ công khai, dạng phòng, nội thất, liên hệ/SĐT.
        Các ý hay còn lại vẫn được viết tự nhiên, có cảm xúc, nhưng không được làm chìm các dòng thông tin quan trọng.
        Mỗi lần tạo một phiên bản mới khác cách mở bài, cách nhấn lợi ích và lời kêu gọi.
        """;
  }

  private String userPrompt(Room room) {
    return """
        Tạo 1 mẫu tin đăng cho thuê phòng/căn hộ thật hút khách từ thông tin sau.
        Hãy viết kết quả bằng tiếng Việt có dấu. Phong cách tham khảo, không sao chép y nguyên:
        %s

        Thông tin ĐƯỢC phép đưa cho khách:
        - Mã phòng: %s
        - Địa chỉ ẩn/khu vực công khai: %s
        - Cách nói về giá: Không đưa giá chính xác. Chỉ nói "giá tốt", "hỗ trợ kéo giá", hoặc "inbox/Zalo để nhận giá tốt".
        - Dạng phòng: %s
        - Nội thất: %s
        - Tiện ích/vị trí có lợi: %s
        - Điểm hay cần nhấn: %s
        - Liên hệ bắt buộc: %s

        Note nội bộ chỉ để kiểm tra độ chính xác, KHÔNG đưa nguyên văn vào tin đăng:
        %s

        Yêu cầu:
        - Mở bài bắt mắt cho khách đang lướt tin.
        - Sau câu mở bài phải có một cụm thông tin rõ ràng, mỗi ý một dòng riêng, theo đúng nhãn:
          📍 Địa chỉ: %s
          🏠 Dạng phòng: %s
          🛋️ Nội thất: %s
          📞 Liên hệ: %s
        - Không được có dòng "Giá thuê:" và không được ghi con số giá cụ thể trong bài.
        - Có thể dùng một dòng mềm như "💸 Giá tốt, inbox/Zalo Mạnh để nhận giá và hỗ trợ kéo giá".
        - Nếu diện tích, tiện ích hoặc điểm hay có dữ liệu thì vẫn đưa vào các dòng/đoạn còn lại cho tự nhiên.
        - Có emoji vừa phải, không quá rối.
        - Độ dài bắt buộc 8-12 dòng, không viết dưới 8 dòng.
        - Chỉ dùng địa chỉ ẩn/khu vực công khai, không gợi ý khách đến gặp chủ trực tiếp.
        - Lời mời cuối bài: inbox/gọi/Zalo/Mess cho Mạnh để được gửi ảnh và hẹn xem phòng.
        - Kết thúc trọn câu, không dừng giữa chừng.
        - Chỉ trả về nội dung mẫu tin, không giải thích thêm.
        - Mã biến thể để tạo mới: %s
        """.formatted(
        STYLE_SAMPLE,
        fallback(room.getCode()),
        fallback(room.getAddress()),
        fallback(room.getLayout()),
        fallback(room.getFurniture()),
        fallback(room.getAmenities()),
        fallback(room.getSellingPoints()),
        contact(room),
        fallback(room.getNote()),
        fallback(room.getAddress()),
        fallback(room.getLayout()),
        fallback(room.getFurniture()),
        contact(room),
        UUID.randomUUID());
  }

  private String contact(Room room) {
    return room.getContact() == null || room.getContact().isBlank() ? DEFAULT_CONTACT : room.getContact();
  }

  private String scrubInternalDetails(String text, Room room) {
    String result = text;
    if (room.getRealAddress() != null && !room.getRealAddress().isBlank()) {
      result = result.replace(room.getRealAddress(), fallback(room.getAddress()));
    }
    return result.replace("0967458281", "0353830297");
  }

  private String fallback(String value) {
    return value == null || value.isBlank() ? "Chưa cung cấp" : value;
  }

  private String extractText(JsonNode response) {
    if (response == null) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    for (JsonNode candidate : response.path("candidates")) {
      for (JsonNode part : candidate.path("content").path("parts")) {
        String text = part.path("text").asText("");
        if (!text.isBlank()) {
          builder.append(text).append("\n");
        }
      }
    }
    return builder.toString();
  }
}
