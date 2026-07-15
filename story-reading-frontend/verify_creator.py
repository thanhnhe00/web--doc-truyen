import os
import time
from playwright.sync_api import sync_playwright, expect

def run_verification():
    os.makedirs("/home/jules/verification/creator", exist_ok=True)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1280, "height": 800})
        page = context.new_page()

        print("1. Navigating to Login Page...")
        page.goto("http://localhost:5173/login")
        page.wait_for_selector("input[placeholder*='tên đăng nhập']")

        # Take a screenshot of login page
        page.screenshot(path="/home/jules/verification/creator/01_login_page.png")

        print("2. Logging in as 'creator'...")
        page.fill("input[placeholder*='tên đăng nhập']", "creator")
        page.fill("input[placeholder*='mật khẩu']", "123456")
        page.locator(".auth-submit").click()

        # Wait for the login redirection to finish
        print("Waiting for redirection...")
        page.wait_for_url("http://localhost:5173/creator")
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/02_creator_dashboard_overview.png")

        # Click on 'Quản lý truyện' tab
        print("4. Switching to Story Management Tab...")
        page.get_by_role("button", name="Quản lý truyện").click()
        page.wait_for_timeout(1500)
        page.screenshot(path="/home/jules/verification/creator/04_story_management_tab.png")

        print("5. Clicking 'Tạo truyện mới'...")
        page.get_by_role("button", name="Tạo truyện mới").click()
        page.wait_for_timeout(1000)
        page.screenshot(path="/home/jules/verification/creator/05_create_story_form_open.png")

        print("6. Filling out Create Story form...")
        page.locator("label:has-text('Tiêu đề') + input").fill("Hành Trình Kỳ Diệu")
        page.locator("label:has-text('Tác giả') + input").fill("Jules Verne")
        page.locator("label:has-text('Mô tả') + textarea").fill("Đây là một câu chuyện vô cùng hấp dẫn và ly kỳ về hành trình phiêu lưu khám phá thế giới bí ẩn của tác giả.")
        page.locator("select").nth(0).select_option("NOVEL") # Content Type
        page.locator("select").nth(1).select_option("16") # Age Rating
        page.locator("select").nth(2).select_option("1") # Select Category (Tiên Hiệp)
        page.wait_for_timeout(1000)
        page.screenshot(path="/home/jules/verification/creator/06_create_story_form_filled.png")

        print("7. Submitting New Story...")
        page.get_by_role("button", name="Tạo truyện", exact=True).click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/07_story_created_success.png")

        print("8. Opening Edit Story Form to verify preloading...")
        # Since 'Hành Trình Kỳ Diệu' is the newly created story, let's find it
        # The list is updated. We locate the manage-item-main and click 'Sửa'
        story_item = page.locator(".my-story-manage-item", has_text="Hành Trình Kỳ Diệu").first
        story_item.locator("button:has-text('Sửa')").first.click()
        page.wait_for_timeout(1500)
        page.screenshot(path="/home/jules/verification/creator/08_edit_story_preloaded.png")

        # Let's verify existing fields are preloaded
        expect(page.locator("input[value='Hành Trình Kỳ Diệu']").first).to_be_visible()
        expect(page.locator("input[value='Jules Verne']").first).to_be_visible()

        print("9. Updating Story data...")
        page.locator(".create-story-form textarea").fill("Đây là một câu chuyện vô cùng hấp dẫn và ly kỳ về hành trình phiêu lưu khám phá thế giới bí ẩn của tác giả (đã cập nhật mô tả).")
        page.get_by_role("button", name="Lưu thay đổi").first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/09_story_updated_success.png")

        print("10. Expanding chapters list for 'Hành Trình Kỳ Diệu'...")
        # We need to find the story item again and click 'Chương'
        story_item = page.locator(".my-story-manage-item", has_text="Hành Trình Kỳ Diệu").first
        story_item.locator("button:has-text('Chương')").first.click()
        page.wait_for_timeout(1500)
        page.screenshot(path="/home/jules/verification/creator/10_chapters_drawer_expanded.png")

        print("11. Clicking 'Thêm chương'...")
        story_item.locator("button:has-text('Thêm chương')").first.click()
        page.wait_for_timeout(1000)
        page.screenshot(path="/home/jules/verification/creator/11_add_chapter_form_open.png")

        print("12. Creating Chapter 1 as DRAFT...")
        page.locator("label:has-text('Số chương') + input").fill("1")
        page.locator("label:has-text('Tiêu đề chương') + input").fill("Chương 1: Khởi Đầu Mới")
        page.locator("textarea[placeholder*='Nhập nội dung chương']").fill("Nội dung vô cùng ly kỳ của chương 1 bắt đầu từ đây...")
        page.wait_for_timeout(1000)
        page.screenshot(path="/home/jules/verification/creator/12_add_chapter_form_filled.png")

        story_item.locator("button:has-text('Tạo chương')").first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/13_chapter1_created_draft.png")

        print("13. Verifying that the newly created chapter is in DRAFT status...")
        expect(page.locator(".chapter-item", has_text="Chương 1: Khởi Đầu Mới").first.locator(".status-chip.draft")).to_be_visible()

        print("14. Testing chapter number validation (duplicate number)...")
        # Click 'Thêm chương' again
        story_item.locator("button:has-text('Thêm chương')").first.click()
        page.wait_for_timeout(1000)
        # Try to create chapter 1 again
        page.locator("label:has-text('Số chương') + input").fill("1")
        page.locator("label:has-text('Tiêu đề chương') + input").fill("Chương 1: Trùng Lặp")
        page.locator("textarea[placeholder*='Nhập nội dung chương']").fill("Nội dung trùng lặp...")
        page.screenshot(path="/home/jules/verification/creator/14_add_duplicate_chapter_filled.png")

        # Let's intercept alert dialog
        def handle_dialog(dialog):
            print(f"Intercepted dialog: {dialog.message}")
            assert "Số chương đã tồn tại" in dialog.message
            dialog.dismiss()

        page.once("dialog", handle_dialog)
        story_item.locator("button:has-text('Tạo chương')").first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/15_duplicate_chapter_rejected.png")

        # Cancel the form
        story_item.locator("button:has-text('Hủy')").first.click()
        page.wait_for_timeout(1000)

        print("15. Editing the DRAFT chapter...")
        chapter_item = page.locator(".chapter-item", has_text="Chương 1: Khởi Đầu Mới").first
        chapter_item.locator("button:has-text('Sửa')").first.click()
        page.wait_for_timeout(1500)
        page.screenshot(path="/home/jules/verification/creator/16_edit_chapter_form_open.png")

        # Change title
        page.locator(".create-chapter-form input[value*='Chương 1']").fill("Chương 1: Khởi Đầu Rực Rỡ")
        page.locator(".create-chapter-form textarea[placeholder*='Nhập nội dung chương']").fill("Nội dung vô cùng ly kỳ của chương 1 bắt đầu từ đây (đã cập nhật nội dung).")
        page.screenshot(path="/home/jules/verification/creator/17_edit_chapter_form_filled.png")

        # Save chapter
        page.locator(".create-chapter-form button:has-text('Lưu thay đổi')").first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/18_chapter_edited_success.png")

        print("16. Submitting the chapter for review (PENDING)...")
        chapter_item = page.locator(".chapter-item", has_text="Chương 1: Khởi Đầu Rực Rỡ").first
        chapter_item.locator("button:has-text('Gửi')").first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/19_chapter_submitted_pending.png")

        # Verify status is now PENDING
        expect(page.locator(".chapter-item", has_text="Chương 1: Khởi Đầu Rực Rỡ").first.locator(".status-chip.pending")).to_be_visible()

        print("17. Creating a temporary chapter to test deletion...")
        story_item.locator("button:has-text('Thêm chương')").first.click()
        page.wait_for_timeout(1000)
        page.locator("label:has-text('Số chương') + input").fill("2")
        page.locator("label:has-text('Tiêu đề chương') + input").fill("Chương 2: Để Xóa")
        page.locator("textarea[placeholder*='Nhập nội dung chương']").fill("Chương này sẽ bị xóa.")
        story_item.locator("button:has-text('Tạo chương')").first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/20_temp_chapter_created.png")

        print("18. Deleting the DRAFT chapter...")
        chapter2_item = page.locator(".chapter-item", has_text="Chương 2: Để Xóa").first

        # Intercept confirm dialog
        page.once("dialog", lambda dialog: dialog.accept())
        chapter2_item.locator(".delete-btn").first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/21_chapter_deleted_success.png")

        # Verify Chapter 2 is deleted
        expect(page.locator(".chapter-item", has_text="Chương 2: Để Xóa").first).not_to_be_visible()

        print("19. Creating a draft story to test story deletion...")
        page.get_by_role("button", name="Tạo truyện mới").first.click()
        page.wait_for_timeout(1000)
        page.locator("label:has-text('Tiêu đề') + input").fill("Truyện Nháp Để Xóa")
        page.locator("select").nth(0).select_option("NOVEL") # Content Type
        page.locator("select").nth(1).select_option("0") # Age Rating
        page.get_by_role("button", name="Tạo truyện", exact=True).first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/22_temp_story_created.png")

        print("20. Deleting the DRAFT story...")
        temp_story_item = page.locator(".my-story-manage-item", has_text="Truyện Nháp Để Xóa").first

        # Intercept confirm dialog
        page.once("dialog", lambda dialog: dialog.accept())
        temp_story_item.locator(".delete-btn").first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/creator/23_story_deleted_success.png")

        # Verify the temp story is deleted
        expect(page.locator(".my-story-manage-item", has_text="Truyện Nháp Để Xóa").first).not_to_be_visible()

        print("E2E Creator flows verification completed successfully!")
        browser.close()

if __name__ == "__main__":
    run_verification()
