import os
import time
from playwright.sync_api import sync_playwright, expect

def run_verification():
    os.makedirs("/home/jules/verification", exist_ok=True)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        # Create context to preserve session storage / local storage
        context = browser.new_context(viewport={"width": 1280, "height": 800})
        page = context.new_page()

        print("1. Navigating to Home Page as Guest...")
        page.goto("http://localhost:5173")
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/01_home_guest.png")

        print("2. Logging in as 'reader'...")
        page.goto("http://localhost:5173/login")
        page.wait_for_selector("input[placeholder*='tên đăng nhập']")
        page.fill("input[placeholder*='tên đăng nhập']", "reader")
        page.fill("input[placeholder*='mật khẩu']", "123456")
        page.locator(".auth-submit").click()

        # Wait for the login redirection to finish
        print("Waiting for redirection...")
        page.wait_for_url("http://localhost:5173/")
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/02_logged_in.png")

        print("3. Navigating to 'Võ Luyện Đỉnh Phong' story detail...")
        # Since we are on home page, click the story "Võ Luyện Đỉnh Phong"
        story_link = page.get_by_text("Võ Luyện Đỉnh Phong").first
        story_link.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/03_story_detail.png")

        print("4. Clicking 'Đọc Từ Đầu'...")
        page.get_by_role("link", name="Đọc Từ Đầu").click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/04_reading_chapter1.png")

        print("5. Adjusting reading settings...")
        # Toggle settings panel
        page.locator(".reading-setting-btn").click()
        page.wait_for_timeout(1000)
        # Toggle Dark Mode
        page.get_by_role("button", name=" Tối").click()
        page.wait_for_timeout(1000)
        page.screenshot(path="/home/jules/verification/05_dark_mode_settings.png")

        print("6. Posting a comment...")
        # Scroll down to comment section
        page.locator(".comment-input").fill("Truyện rất hay, mong ra chap mới!")
        page.get_by_role("button", name="Gửi bình luận").click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/06_comment_posted.png")

        print("7. Clicking 'Chap Sau' to read Chapter 2...")
        # Close settings panel if open
        # Let's find "Chap Sau" button
        next_button = page.get_by_role("button", name="Chap Sau")
        next_button.first.click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/07_reading_chapter2.png")

        print("8. Returning to Story Detail page to verify 'Đọc tiếp' prompt & dismiss...")
        # Go back to story detail page
        page.locator(".reading-breadcrumb").click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/08_story_detail_resume_prompt.png")

        # Let's verify "Đọc tiếp" and "X" (dismiss) are visible.
        expect(page.get_by_text("Đọc tiếp - Chương 2")).to_be_visible()
        # Dismiss it
        page.locator("button[title='Bỏ qua']").click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/09_resume_prompt_dismissed.png")

        # Now "Đọc Từ Đầu" should be visible instead of "Đọc tiếp"
        expect(page.get_by_role("link", name="Đọc Từ Đầu")).to_be_visible()

        print("9. Verifying personalized recommendations on Home Page...")
        page.goto("http://localhost:5173")
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/10_home_recommendations.png")

        print("10. Reporting a story...")
        page.goto("http://localhost:5173/story/1")
        page.wait_for_timeout(1000)
        page.get_by_role("button", name="Báo cáo").click()
        page.wait_for_timeout(1000)
        # Fill reason
        page.locator(".report-textarea").fill("Nội dung spam và trùng lặp nhiều")
        page.get_by_role("button", name="Gửi báo cáo").click()
        page.wait_for_timeout(2500)
        page.screenshot(path="/home/jules/verification/11_report_submitted.png")

        # Try to report again
        page.get_by_role("button", name="Báo cáo").click()
        page.wait_for_timeout(1000)
        page.locator(".report-textarea").fill("Nội dung spam")
        page.get_by_role("button", name="Gửi báo cáo").click()
        page.wait_for_timeout(2500)
        page.screenshot(path="/home/jules/verification/12_report_duplicate_conflict.png")

        print("Verification completed successfully!")
        browser.close()

if __name__ == "__main__":
    run_verification()
