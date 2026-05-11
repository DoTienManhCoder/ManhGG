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
  private static final String DEFAULT_CONTACT = "Tiến Mạnh - 0373.907.159 (có Zalo)";
  private static final String STYLE_SAMPLE = """
      🔥🔥 CHO THUÊ CĂN HỘ #1PN TÁCH BẾP - GIẶT RIÊNG NGAY TÂN BÌNH - GẦN ETOWN CỘNG HOÀ
      ✨Nội thất: máy lạnh, tủ lạnh, tủ đồ, nệm, gra gối, sofa, kệ bếp, tủ bếp, máy nước nóng, tivi, lò vi sóng, bếp từ,...
      ▪️Toà thang máy, hầm xe rộng
      ▪️Không chung chủ, Giờ giấc tự do
      ▪️Có dọn phòng
      ▪️Ra vào vân tay, camera 24/24
      📍 Địa chỉ: Tân Kì Tân Quý, Phường Tân Quý, Tân Phú
      ☎ Gọi ngay: 0373.907.159 Tiến Mạnh (có ZALO) để xem phòng
      #CanHoChoThue #PhongTroTanBinh #PhongDepGiaTot #ChoThuePhong
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
            "temperature", 0.65,
            "topP", 0.85,
            "maxOutputTokens", 700,
            "thinkingConfig", Map.of("thinkingBudget", 0)));
  }

  private String systemInstruction() {
    return """
        Bạn viết tin đăng cho thuê phòng/căn hộ bằng tiếng Việt có dấu, dùng đăng Facebook/Zalo.
        Viết ngắn gọn, rõ ý, giống mẫu tin thực tế của môi giới: có emoji vừa phải, gạch đầu dòng, không văn chương hoa mỹ.
        Chỉ dùng thông tin có trong dữ liệu và địa chỉ công khai. Không đưa địa chỉ đúng nội bộ, thông tin chủ nhà, điểm xấu, phí/lưu ý nội bộ.
        Không ghi giá thuê chính xác; chỉ nói giá tốt, inbox/Zalo để nhận giá hoặc hỗ trợ kéo giá.
        Ưu tiên các ý: dạng phòng, nội thất, tiện ích toà, giờ giấc, an ninh, vị trí, liên hệ.
        Tin cuối cùng phải dài khoảng 500-600 ký tự, không dài dòng.
        """;
  }

  private String userPrompt(Room room) {
    return """
        Tạo 1 mẫu tin đăng cho thuê phòng/căn hộ ngắn gọn từ thông tin sau.
        Phong cách tham khảo, không sao chép y nguyên:
        %s

        Thông tin được phép đưa cho khách:
        - Mã phòng: %s
        - Địa chỉ ẩn/khu vực công khai: %s
        - Giá: không đưa số giá chính xác; chỉ nói giá tốt/inbox/Zalo để nhận giá.
        - Dạng phòng: %s
        - Nội thất: %s
        - Tiện ích/vị trí có lợi: %s
        - Điểm hay cần nhấn: %s
        - Liên hệ: %s

        Note nội bộ chỉ để kiểm tra độ chính xác, KHÔNG đưa nguyên văn vào tin đăng:
        %s

        Yêu cầu:
        - Độ dài 500-600 ký tự.
        - Mở bài 1 dòng dạng: 🔥🔥 CHO THUÊ ... + dạng phòng/khu vực/điểm nổi bật.
        - Không đưa danh sách nội thất vào tiêu đề/mở bài.
        - Các dòng sau dùng bullet ngắn như ✨, ▪️, 📍, ☎.
        - Bắt buộc tách nội thất thành 1 gạch đầu dòng riêng nếu có dữ liệu, dạng: ✨Nội thất: ...
        - Tiện ích/toà nhà/an ninh/giờ giấc mỗi ý ngắn một dòng.
        - Cuối bài có số điện thoại/Zalo và tên liên hệ.
        - Thêm 4-6 hashtag cuối bài, viết liền không dấu hoặc có dấu đều được, ưu tiên theo khu vực/dạng phòng/tiện ích. Ví dụ: #CanHoChoThue #PhongTroGiaTot #PhongDep #ChoThuePhong.
        - Không dùng từ hoa mỹ như "đẳng cấp", "sang trọng bậc nhất", "siêu phẩm" nếu dữ liệu không có.
        - Không ghi giá thuê chính xác, không đưa địa chỉ đúng nội bộ.
        - Chỉ trả về nội dung mẫu tin, không giải thích.
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
    return result
        .replace("0967458281", "0373.907.159")
        .replace("0353830297", "0373.907.159");
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
