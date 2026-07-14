package com.storyreading.storyreadingbackend.component;

import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.entity.enums.ContentType;
import com.storyreading.storyreadingbackend.entity.enums.UserRole;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StoryRepository storyRepository;
    private final ChapterRepository chapterRepository;
    private final StoryCategoryRepository storyCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ThemeCollectionRepository themeCollectionRepository;
    private final CollectionStoryRepository collectionStoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // 1. Tạo Users
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setBirthDate(LocalDate.of(1990, 1, 1));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);

            User creator1 = new User();
            creator1.setUsername("creator");
            creator1.setEmail("creator@example.com");
            creator1.setPassword(passwordEncoder.encode("123456"));
            creator1.setBirthDate(LocalDate.of(1995, 6, 15));
            creator1.setRole(UserRole.CREATOR);
            userRepository.save(creator1);

            User creator2 = new User();
            creator2.setUsername("tacgia");
            creator2.setEmail("tacgia@example.com");
            creator2.setPassword(passwordEncoder.encode("123456"));
            creator2.setBirthDate(LocalDate.of(1988, 3, 20));
            creator2.setRole(UserRole.CREATOR);
            userRepository.save(creator2);

            User reader = new User();
            reader.setUsername("reader");
            reader.setEmail("reader@example.com");
            reader.setPassword(passwordEncoder.encode("123456"));
            reader.setBirthDate(LocalDate.of(2000, 12, 1));
            reader.setRole(UserRole.READER);
            userRepository.save(reader);

            // 2. Tạo Categories
            Category tienHiep = new Category(); tienHiep.setName("Tiên Hiệp"); categoryRepository.save(tienHiep);
            Category kiemHiep = new Category(); kiemHiep.setName("Kiếm Hiệp"); categoryRepository.save(kiemHiep);
            Category xuyenKhong = new Category(); xuyenKhong.setName("Xuyên Không"); categoryRepository.save(xuyenKhong);
            Category huyenHuyen = new Category(); huyenHuyen.setName("Huyền Huyễn"); categoryRepository.save(huyenHuyen);
            Category tinhCam = new Category(); tinhCam.setName("Tình Cảm"); categoryRepository.save(tinhCam);
            Category hocDuong = new Category(); hocDuong.setName("Học Đường"); categoryRepository.save(hocDuong);

            // 3. Tạo Stories với nội dung thật
            // --- Story 1: Tiên Hiệp ---
            Story story1 = new Story();
            story1.setTitle("Võ Luyện Đỉnh Phong");
            story1.setAuthor("Mạc Mặc");
            story1.setDescription("Truyện kể về hành trình tu luyện đầy gian khổ của Dương Khai. Sinh ra trong một gia tộc nhỏ, sở hữu huyết mạch đặc biệt, Dương Khai từng bước bước lên đỉnh cao võ đạo, trở thành bất khả chiến bại.");
            story1.setContentType(ContentType.NOVEL);
            story1.setAgeRating(0);
            story1.setStatus(ApprovalStatus.PUBLISHED);
            story1.setCreator(creator1);
            story1.setCoverImage("https://res.cloudinary.com/demo/image/upload/v1367916299/sample.jpg");
            storyRepository.save(story1);

            // --- Story 2: Kiếm Hiệp ---
            Story story2 = new Story();
            story2.setTitle("Đấu Phá Thương Khung");
            story2.setAuthor("Thiên Tàm Thổ Đậu");
            story2.setDescription("Tiêu Viêm, một thiên tài tu luyện đột nhiên mất đi khả năng tu luyện. Vì lời hứa với vợ chưa cưới, anh quyết tâm tìm lại hào quang đã mất. Hành trình tìm kiếm Dị Hỏa đầy cam go bắt đầu từ đây.");
            story2.setContentType(ContentType.NOVEL);
            story2.setAgeRating(0);
            story2.setStatus(ApprovalStatus.PUBLISHED);
            story2.setCreator(creator1);
            story2.setCoverImage("https://res.cloudinary.com/demo/image/upload/v1367916295/sample.jpg");
            storyRepository.save(story2);

            // --- Story 3: Xuyên Không ---
            Story story3 = new Story();
            story3.setTitle("Toàn Trí Độc Giả");
            story3.setAuthor("Sing-shong");
            story3.setDescription("Kim Dokja là độc giả duy nhất đọc đến chương cuối cùng của bộ tiểu thuyết mạng 'Ba Cách Sống'. Khi thế giới thực biến thành bối cảnh tiểu thuyết, anh phải sử dụng kiến thức từ 3149 chương để sống sót.");
            story3.setContentType(ContentType.COMIC);
            story3.setAgeRating(16);
            story3.setStatus(ApprovalStatus.PUBLISHED);
            story3.setCreator(creator2);
            story3.setCoverImage("https://res.cloudinary.com/demo/image/upload/v1367916283/sample.jpg");
            storyRepository.save(story3);

            // --- Story 4: Huyền Huyễn ---
            Story story4 = new Story();
            story4.setTitle("Phàm Nhân Tu Tiên");
            story4.setAuthor("Vong Ngữ");
            story4.setDescription("Hàn Lập, một thanh niên nhà nghèo tình cờ được thu nhận vào tiểu môn phái. Với ngộ tính bình thường nhưng kiên trì bền bỉ, anh dần bước trên con đường tu tiên đầy hiểm trở.");
            story4.setContentType(ContentType.NOVEL);
            story4.setAgeRating(0);
            story4.setStatus(ApprovalStatus.PUBLISHED);
            story4.setCreator(creator1);
            story4.setCoverImage("https://res.cloudinary.com/demo/image/upload/v1367916359/sample.jpg");
            storyRepository.save(story4);

            // --- Story 5: Tình Cảm ---
            Story story5 = new Story();
            story5.setTitle("Nguyên Tôn");
            story5.setAuthor("Thiên Tàm Thổ Đậu");
            story5.setDescription("Chu Nguyên, hoàng tử của Đại Chu, bị đoạt thánh long khí từ nhỏ. Được sư phụ phù trợ, anh tu luyện nguyên khí, luyện đan, viết trận pháp,一步一步 trở lại đỉnh cao.");
            story5.setContentType(ContentType.NOVEL);
            story5.setAgeRating(0);
            story5.setStatus(ApprovalStatus.PUBLISHED);
            story5.setCreator(creator2);
            story5.setCoverImage("https://res.cloudinary.com/demo/image/upload/v1367916267/sample.jpg");
            storyRepository.save(story5);

            // Gán thể loại
            StoryCategory sc1 = new StoryCategory(); sc1.setStory(story1); sc1.setCategory(tienHiep); storyCategoryRepository.save(sc1);
            StoryCategory sc2 = new StoryCategory(); sc2.setStory(story1); sc2.setCategory(huyenHuyen); storyCategoryRepository.save(sc2);
            StoryCategory sc3 = new StoryCategory(); sc3.setStory(story2); sc3.setCategory(tienHiep); storyCategoryRepository.save(sc3);
            StoryCategory sc4 = new StoryCategory(); sc4.setStory(story2); sc4.setCategory(kiemHiep); storyCategoryRepository.save(sc4);
            StoryCategory sc5 = new StoryCategory(); sc5.setStory(story3); sc5.setCategory(xuyenKhong); storyCategoryRepository.save(sc5);
            StoryCategory sc6 = new StoryCategory(); sc6.setStory(story4); sc6.setCategory(tienHiep); storyCategoryRepository.save(sc6);
            StoryCategory sc7 = new StoryCategory(); sc7.setStory(story4); sc7.setCategory(huyenHuyen); storyCategoryRepository.save(sc7);
            StoryCategory sc8 = new StoryCategory(); sc8.setStory(story5); sc8.setCategory(tienHiep); storyCategoryRepository.save(sc8);
            StoryCategory sc9 = new StoryCategory(); sc9.setStory(story5); sc9.setCategory(tinhCam); storyCategoryRepository.save(sc9);

            // 4. Tạo Chapters với nội dung thật
            // --- Story 1: Võ Luyện Đỉnh Phong ---
            String[] vldpContents = {
                "Chương 1: Khởi đầu\n\nDương Khai ngồi trong phòng tu luyện, ánh mắt tập trung vào quyển bí kíp cổ xưa. Từ khi gia nhập Tinh Không Tông, anh đã nỗ lực không ngừng. Hôm nay, cuối cùng anh cũng đã cảm nhận được khí tức nguyên lực đầu tiên trong cơ thể.\n\n'Thuật Đạo Chi Lộ, gian nan vạn phần...' Dương Khai lẩm bẩm, đôi mắt sáng lên. Anh biết rằng, con đường phía trước còn rất dài.",
                "Chương 2: Thử thách\n\nSáng sớm, tiếng chuông vang lên trong sân huấn luyện. Tất cả đệ tử tập trung, ánh mắt nhìn về phía giảng sư. Hôm nay là bài kiểm tra định kỳ hàng tháng.\n\nDương Khai đứng giữa sân, nhìn các đối thủ xung quanh. Một số người khinh thường nhìn anh, nhưng anh không quan tâm. Anh biết khả năng của mình.\n\nKết quả: Dương Khai xếp thứ ba trong đợt kiểm tra, vượt qua nhiều người đã tu luyện lâu hơn.",
                "Chương 3: Kỳ ngộ\n\nĐêm khuya, Dương Khai bí mật rời phòng, đến khu rừng phía sau núi. Tại đây, anh tìm thấy một hang động ẩn sau thác nước.\n\nBên trong hang động, một viên ngọc phát sáng tỏa ra ánh sáng vàng nhạt. Khi tay anh chạm vào viên ngọc, một luồng năng lượng mạnh mẽ chảy vào cơ thể.\n\n'Đây là... Nguyên Thạch?' Tim Dương Khai đập nhanh. Anh biết mình đã tìm được bảo vật.",
                "Chương 4: Đột phá\n\nSau khi hấp thu Nguyên Thạch, tu vi của Dương Khai tăng lên đáng kể. Từ tầng 3直接 nhảy lên tầng 5, khiến mọi người trong tông môn đều kinh ngạc.\n\n'Không thể tin được!' Giảng sư nhìn kết quả kiểm tra, đôi mắt mở to. 'Hắn đã đột phá hai tầng chỉ trong một tháng?'\n\nDương Khai mỉm cười, nhưng trong lòng biết rằng, đằng sau thành công này là những đêm không ngủ và nỗ lực không ngừng.",
                "Chương 5: Đối thủ\n\nHọc kỳ mới bắt đầu, và đối thủ mới xuất hiện. Mạc Vũ, thiên tài của gia tộc lớn, tuyên bố sẽ vượt qua mọi người trong đợt thi cuối năm.\n\nDương Khai nhìn Mạc Vũ từ xa, ánh mắt không sợ hãi. Anh biết rằng, để trở nên mạnh mẽ, anh cần phải vượt qua mọi thử thách.\n\n'Đấu trường, đây là nơi ta sẽ chứng minh bản thân.' Dương Khai tự nhủ, bước vào buổi huấn luyện đầu tiên."
            };
            String[] dptkContents = {
                "Chương 1: Thiên tài sa ngã\n\nTiêu Viêm ngồi trong góc phòng, đôi mắt vô hồn nhìn ra ngoài cửa sổ. Ba năm trước, anh là thiên tài được mọi người ngưỡng mộ. Nhưng giờ đây, khả năng tu luyện của anh đã biến mất.\n\n'Dãmanh!' Một giọng nói khinh thường vang lên. 'Thiên tài gì mà ba năm không tiến bộ được gì.'\n\nTiêu Viêm nắm chặt tay, nhưng không nói gì. Anh biết rằng, giờ là lúc anh cần bình tĩnh nhất.",
                "Chương 2: Dị Hỏa\n\nTrong giấc mơ, một giọng nói bí ẩn vang lên: 'Tiêu Viêm, nếu muốn lấy lại tất cả, hãy tìm Dị Hỏa...'\n\nKhi tỉnh dậy, Tiêu Viemer bắt đầu hành trình tìm kiếm Dị Hỏa. Anh biết rằng, đây là cơ hội duy nhất để khôi phục khả năng tu luyện.\n\nĐường đi đầy khó khăn, nhưng ý chí của anh không bao giờ lung lay.",
                "Chương 3: Cuộc gặp gỡ\n\nTrên đường đi, Tiêu Viemer gặp Tiểu Yêu Tiên, một cô gái bí ẩn với khả năng tu luyện đáng kinh ngạc.\n\n'Ngươi cũng đang tìm Dị Hỏa?' Tiểu Yêu Tiên nhìn anh với ánh mắt tò mò.\n\nHai người quyết định hợp tác, và từ đây, hành trình trở nên thú vị hơn bao giờ hết.",
                "Chương 4: Thử thách\n\nVào sâu trong rừng cổ thụ, hai người đối mặt với yêu thú khổng lồ. Tiêu Viemer sử dụng kỹ năng chiến đấu của mình, kết hợp với sức mạnh mới từ Dị Hỏa.\n\n'Cẩn thận!' Tiểu Yêu Tiên hét lên khi yêu thú lao tới.\n\nTiêu Viemer né tránh và phản công, cuối cùng đánh bại được yêu thú. Anh đã tiến bộ rất nhiều trên hành trình tìm kiếm Dị Hỏa.",
                "Chương 5: Hy vọng\n\nSau nhiều ngày tìm kiếm, cuối cùng Tiêu Viemer cũng tìm thấy Dị Hỏa đầu tiên. Khi hấp thu ngọn lửa kỳ lạ, khả năng tu luyện của anh bắt đầu khôi phục.\n\n'Cảm ơn...' Tiêu Viemer thì thầm, cảm nhận sức mạnh chảy trong cơ thể.\n\nHành trình phía trước còn dài, nhưng giờ đây anh đã có hy vọng. Và bên cạnh anh, Tiểu Yêu Tiên luôn ở bên."
            };

            for (int i = 0; i < 5; i++) {
                Chapter c1 = new Chapter();
                c1.setStory(story1);
                c1.setChapterNumber(i + 1);
                c1.setTitle(vldpContents[i].split("\n")[0]);
                c1.setContent(vldpContents[i]);
                c1.setStatus(ApprovalStatus.PUBLISHED);
                chapterRepository.save(c1);

                Chapter c2 = new Chapter();
                c2.setStory(story2);
                c2.setChapterNumber(i + 1);
                c2.setTitle(dptkContents[i].split("\n")[0]);
                c2.setContent(dptkContents[i]);
                c2.setStatus(ApprovalStatus.PUBLISHED);
                chapterRepository.save(c2);
            }

            // Story 3, 4, 5: mỗi故事 3 chương
            String[] ttđgContents = {
                "Chương 1: Bắt đầu lại\n\nKim Dokja mở mắt,发现自己 nằm trên giường bệnh. TV đang phát tin tức về những hiện tượng bí ẩn. Anh nhận ra rằng, thế giới đã thay đổi.\n\n'Chương 1: Bắt đầu' - Một thông báo xuất hiện trước mặt. 'Hoàn thành thử thách đầu tiên để nhận phần thưởng.'\n\nKim Dokja mỉm cười. Anh đã đọc đến chương 3149, nên anh biết chính xác những gì sắp xảy ra.",
                "Chương 2: Đối mặt\n\nThử thách đầu tiên xuất hiện: Con rắn khổng lồ chặn đường. Những người xung quanh hoảng sợ, nhưng Kim Dokja bình tĩnh.\n\n'Đoạn 3, chương 2: Con rắn có điểm yếu ở bụng.' Anh nhớ lại nội dung tiểu thuyết.\n\nVới kiến thức từ 3149 chương, Kim Dokja dễ dàng vượt qua thử thách.",
                "Chương 3: Đồng đội\n\nSau khi vượt qua thử thách, Kim Dokja gặp được Han Sooyoung, một nhà văn đang tìm cách sống sót.\n\n'Ngươi có vẻ biết trước mọi thứ.' Han Sooyoung nhìn anh với ánh mắt nghi ngờ.\n\n'Chỉ là may mắn thôi.' Kim Dokja mỉm cười, nhưng trong lòng biết rằng, kiến thức từ tiểu thuyết là lợi thế lớn nhất của anh."
            };
            String[] pnttContents = {
                "Chương 1: Tiểu môn phái\n\nHàn Lập đứng trước cổng Tiểu Thiên Tông, đôi mắt tò mò nhìn xung quanh. Anh là thanh niên nhà nghèo, được người quen giới thiệu vào đây.\n\n'Hàn Lập, từ nay ngươi là đệ tử ngoại môn.' Thủ lĩnh nói ngắn gọn.\n\nHàn Lập cúi đầu, quyết tâm sẽ nỗ lực hết mình để không phụ lòng kỳ vọng.",
                "Chương 2: Tu luyện\n\nNgày ngày, Hàn Lập chăm chỉ tu luyện. Dù ngộ tính không cao, nhưng anh luôn là người dậy sớm nhất và ngủ muộn nhất.\n\n'Không có gì là không thể.' Anh tự nhủ mỗi khi mệt mỏi.\n\nSau một năm, Hàn Lập từ ngoại môn lên nội môn, khiến nhiều người bất ngờ.",
                "Chương 3: Nhiệm vụ đầu tiên\n\nHàn Lập nhận nhiệm vụ đầu tiên: Thu thập linh dược trong rừng cổ thụ.\n\nTrong rừng, anh đối mặt với yêu thú nhỏ, nhưng nhờ sự cẩn thận, anh đã hoàn thành nhiệm vụ an toàn.\n\n'Linh thảo này...' Anh phát hiện một loại linh dược quý hiếm. 'Có thể giúp ta tăng tu vi.'\n\nAnh quyết định giữ bí mật về phát hiện này."
            };
            String[] nguyenTonContents = {
                "Chương 1: Hoàng tử mất nước\n\nChu Nguyên ngồi trong túp lều nhỏ, đôi mắt nhìn ra bầu trời đêm. Ba năm trước, anh là hoàng tử của Đại Chu. Nhưng giờ đây, anh chỉ là kẻ chạy trốn.\n\n'Hoàng tử, người không thể từ bỏ.' Một giọng nói vang lên.\n\nChu Nguyên nắm chặt tay. 'Ta sẽ lấy lại tất cả. Ta thề.'",
                "Chương 2: Nguyên khí\n\nĐược sư phụ chỉ dạy, Chu Nguyên bắt đầu tu luyện nguyên khí. Dù bị đoạt thánh long khí từ nhỏ, nhưng anh có ý chí sắt đá.\n\n'Mỗi ngày đều phải tiến bộ.' Anh tự nhủ.\n\nSau ba tháng, Chu Nguyên đã nắm vững nguyên khí cơ bản.",
                "Chương 3: Luyện đan\n\nNgoài tu luyện nguyên khí, Chu Nguyên còn học luyện đan. Đây là kỹ năng quan trọng để hỗ trợ tu luyện.\n\n'Hỏa候 phải chính xác.' Thầy dạy nhấn mạnh.\n\nChu Nguyên tập trung, điều khiển ngọn lửa. Lần đầu tiên, viên đan dược thành công."
            };

            for (int i = 0; i < 3; i++) {
                Chapter c3 = new Chapter();
                c3.setStory(story3);
                c3.setChapterNumber(i + 1);
                c3.setTitle(ttđgContents[i].split("\n")[0]);
                c3.setContent(ttđgContents[i]);
                c3.setStatus(ApprovalStatus.PUBLISHED);
                chapterRepository.save(c3);

                Chapter c4 = new Chapter();
                c4.setStory(story4);
                c4.setChapterNumber(i + 1);
                c4.setTitle(pnttContents[i].split("\n")[0]);
                c4.setContent(pnttContents[i]);
                c4.setStatus(ApprovalStatus.PUBLISHED);
                chapterRepository.save(c4);

                Chapter c5 = new Chapter();
                c5.setStory(story5);
                c5.setChapterNumber(i + 1);
                c5.setTitle(nguyenTonContents[i].split("\n")[0]);
                c5.setContent(nguyenTonContents[i]);
                c5.setStatus(ApprovalStatus.PUBLISHED);
                chapterRepository.save(c5);
            }

            System.out.println("====== SEED DATA SUCCESS: 5 stories, 21 chapters ======");

            // 5. Theme Collections
            ThemeCollection col1 = new ThemeCollection();
            col1.setName("Truyện Tiên Hiệp Hay Nhất");
            col1.setDescription("Tuyển chọn những bộ truyện tiên hiệp được yêu thích nhất");
            themeCollectionRepository.save(col1);
            collectionStoryRepository.save(buildColStory(col1, story1));
            collectionStoryRepository.save(buildColStory(col1, story2));
            collectionStoryRepository.save(buildColStory(col1, story4));

            ThemeCollection col2 = new ThemeCollection();
            col2.setName("Truyện穿越 Ngắn");
            col2.setDescription("Những bộ truyện xuyên không ngắn gọn, dễ đọc");
            themeCollectionRepository.save(col2);
            collectionStoryRepository.save(buildColStory(col2, story3));

            ThemeCollection col3 = new ThemeCollection();
            col3.setName("Truyện Học Đường");
            col3.setDescription("Truyện dành cho lứa tuổi học trò");
            themeCollectionRepository.save(col3);
            collectionStoryRepository.save(buildColStory(col3, story5));
        }
    }

    private CollectionStory buildColStory(ThemeCollection col, Story story) {
        CollectionStory cs = new CollectionStory();
        cs.setCollection(col);
        cs.setStory(story);
        return cs;
    }
}
