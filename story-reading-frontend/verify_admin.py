import os
import time
from playwright.sync_api import sync_playwright, expect

def run_verification():
    print("Starting E2E Admin flows verification...")
    os.makedirs("/home/jules/verification/admin", exist_ok=True)

    with sync_playwright() as p:
        print("Launching browser...")
        browser = p.chromium.launch(headless=True)
        # Create context to preserve session storage / local storage
        context = browser.new_context(viewport={"width": 1280, "height": 800})
        page = context.new_page()

        print("1. Logging in as 'reader' to post a comment...")
        page.goto("http://localhost:5173/login")
        page.wait_for_selector("input[placeholder*='tên đăng nhập']")
        page.fill("input[placeholder*='tên đăng nhập']", "reader")
        page.fill("input[placeholder*='mật khẩu']", "123456")
        page.locator(".auth-submit").click()
        page.wait_for_url("http://localhost:5173/")
        page.wait_for_timeout(2000)

        print("2. Clicking 'Võ Luyện Đỉnh Phong' on home page...")
        story_link = page.get_by_text("Võ Luyện Đỉnh Phong").first
        story_link.click()
        page.wait_for_timeout(2000)

        # Take screenshot of story detail page
        page.screenshot(path="/home/jules/verification/admin/debug_story_detail.png")
        print("Page URL after clicking:", page.url)

        # Click either "Đọc Từ Đầu" or "Đọc tiếp - Chương X" (robust check)
        page.locator("a:has-text('Đọc')").first.click()
        page.wait_for_timeout(2000)

        # Intercept comment creation response to get comment ID
        print("Posting a comment and capturing comment ID...")
        with page.expect_response("**/comments") as response_info:
            page.locator(".comment-input").fill("Bình luận này sẽ bị báo cáo!")
            page.get_by_role("button", name="Gửi bình luận").click()

        response = response_info.value
        comment_data = response.json()
        comment_id = comment_data.get("commentId")
        print(f"Created comment with ID: {comment_id}")
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/admin/01_comment_posted.png")

        # Report the comment via page.evaluate
        print(f"Reporting comment #{comment_id} via API...")
        page.evaluate(f"""
            fetch('http://localhost:8081/api/reports', {{
                method: 'POST',
                headers: {{
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('token')
                }},
                body: JSON.stringify({{
                    targetType: 'COMMENT',
                    targetId: {comment_id},
                    reason: 'Spam và quấy rối'
                }})
            }})
        """)
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/admin/02_comment_reported.png")

        # Log out programmatically
        print("Logging out 'reader' programmatically...")
        page.evaluate("localStorage.clear(); sessionStorage.clear();")
        page.goto("http://localhost:5173/")
        page.wait_for_timeout(1500)

        print("3. Logging in as 'admin'...")
        page.goto("http://localhost:5173/login")
        page.wait_for_selector("input[placeholder*='tên đăng nhập']")
        page.fill("input[placeholder*='tên đăng nhập']", "admin")
        page.fill("input[placeholder*='mật khẩu']", "123456")
        page.locator(".auth-submit").click()
        # Admin is automatically redirected to /admin
        page.wait_for_url("http://localhost:5173/admin")
        page.wait_for_timeout(1500)
        page.screenshot(path="/home/jules/verification/admin/03_admin_dashboard.png")

        print("4. Testing User Management Search (FR14)...")
        page.get_by_role("button", name="Quản lý user").click()
        page.wait_for_timeout(1000)
        page.screenshot(path="/home/jules/verification/admin/04_user_management_tab.png")

        # Enter "creator" in the search box
        page.locator("input[placeholder*='Tìm kiếm theo tên hoặc email']").fill("creator")
        page.get_by_role("button", name="Tìm kiếm").click()
        page.wait_for_timeout(1500)
        page.screenshot(path="/home/jules/verification/admin/05_user_search_creator_results.png")

        # Verify only creator accounts are shown (or only those matching query)
        expect(page.get_by_text("creator@example.com")).to_be_visible()
        expect(page.get_by_text("admin@example.com")).not_to_be_visible()

        # Clear search
        page.get_by_role("button", name="Xóa lọc").click()
        page.wait_for_timeout(1000)
        expect(page.get_by_text("admin@example.com")).to_be_visible()

        print("5. Testing Category Deletion Constraint (FR15)...")
        page.get_by_role("button", name="Thể loại").click()
        page.wait_for_timeout(1000)
        page.screenshot(path="/home/jules/verification/admin/06_category_management_tab.png")

        # Try to delete "Tiên Hiệp" (ID 1), which is used by "Võ Luyện Đỉnh Phong"
        category_row = page.locator("tr", has_text="Tiên Hiệp").first

        # Unified dialog handler for sequential confirm & alert dialogs
        def handle_dialog(dialog):
            print(f"Intercepted dialog: type={dialog.type}, message={dialog.message}")
            if dialog.type == "confirm":
                dialog.accept()
            elif dialog.type == "alert":
                assert "Thể loại đang được sử dụng bởi truyện" in dialog.message
                dialog.dismiss()

        page.on("dialog", handle_dialog)
        category_row.get_by_role("button", name="Xóa").click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/admin/07_category_deletion_rejected.png")
        page.remove_listener("dialog", handle_dialog)

        print("6. Testing Report Management & Comment Hide/Unhide (FR13)...")
        page.get_by_role("button", name="Báo cáo").click()
        page.wait_for_timeout(1500)
        page.screenshot(path="/home/jules/verification/admin/08_reports_tab_with_comment_details.png")

        # Find the comment report box
        report_item = page.locator(".report-item", has_text="Bình luận này sẽ bị báo cáo!").first
        expect(report_item).to_be_visible()
        expect(report_item.get_by_text("Đang hiển thị")).to_be_visible()

        # Click "Ẩn bình luận"
        print("Hiding the reported comment...")
        report_item.get_by_role("button", name="Ẩn bình luận").click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/admin/09_report_comment_hidden.png")
        expect(report_item.get_by_text("Đang ẩn")).to_be_visible()

        # Click "Khôi phục bình luận"
        print("Restoring the reported comment...")
        report_item.get_by_role("button", name="Khôi phục bình luận").click()
        page.wait_for_timeout(2000)
        page.screenshot(path="/home/jules/verification/admin/10_report_comment_restored.png")
        expect(report_item.get_by_text("Đang hiển thị")).to_be_visible()

        # Get count of reports containing our comment before resolving
        count_before = page.locator(".report-item", has_text="Bình luận này sẽ bị báo cáo!").count()
        print(f"Number of reports before resolving: {count_before}")

        # Finally, mark report as resolved
        print("Marking report as resolved...")
        report_item.get_by_role("button", name="Xử lý").click()
        page.wait_for_timeout(1500)
        page.screenshot(path="/home/jules/verification/admin/11_report_resolved_removed.png")

        # Assert count has decreased
        count_after = page.locator(".report-item", has_text="Bình luận này sẽ bị báo cáo!").count()
        print(f"Number of reports after resolving: {count_after}")
        assert count_after == count_before - 1

        print("E2E Admin flows verification completed successfully!")
        browser.close()

if __name__ == "__main__":
    run_verification()
