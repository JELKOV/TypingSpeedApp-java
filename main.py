import tkinter as tk
from tkinter import messagebox
import requests
import random
import os
import shutil
import time
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

class TypingSpeedApp:
    def __init__(self, root):
        self.root = root
        self.root.title("타이핑 속도 테스트")
        self.root.attributes("-fullscreen", True)  # 전체화면 모드
        self.root.bind("<Escape>", self.exit_fullscreen)  # Esc 키로 전체화면 종료

        # 초기 변수
        self.total_time = 600  # 제한 시간 10분
        self.timer = self.total_time
        self.score = 0
        self.correct_problems = 0  # 맞춘 문제 개수
        self.problem_count = 0  # 현재 문제 번호
        self.max_problems = 5  # 최대 문제 수
        self.assets_folder = "assets"
        self.cached_files = []  # 캐싱된 파일 리스트
        self.sample_text = ""  # 초기 샘플 텍스트
        self.start_time = None  # 타이핑 시작 시간

        # UI 구성
        self.create_widgets()

        # 파일 로드 (assets에 파일이 있으면 API 호출 생략)
        if os.path.exists(self.assets_folder) and os.listdir(self.assets_folder):
            self.load_cached_files()
        else:
            self.load_files_from_github()

        # 첫 문제 출력
        self.next_problem()

    def create_widgets(self):
        main_frame = tk.Frame(self.root)
        main_frame.pack(expand=True, fill=tk.BOTH, padx=20, pady=20)

        # 코드 표시 영역 (문제 칸)
        problem_frame = tk.Frame(main_frame)
        problem_frame.grid(row=0, column=0, padx=10, pady=10, sticky="nsew")

        self.text_label = tk.Text(problem_frame, wrap=tk.WORD, font=("Courier", 16), height=35, width=50)
        self.text_label.config(state=tk.DISABLED)  # 텍스트 편집 불가
        self.text_label.pack(expand=True, fill=tk.BOTH)

        # 타이핑 입력 영역 (입력 칸)
        entry_frame = tk.Frame(main_frame)
        entry_frame.grid(row=0, column=1, padx=10, pady=10, sticky="nsew")

        self.entry = tk.Text(entry_frame, wrap=tk.WORD, font=("Courier", 16), height=35, width=50)
        self.entry.bind("<KeyRelease>", self.check_typing)
        self.entry.pack(expand=True, fill=tk.BOTH)

        # 버튼 영역 (하단)
        bottom_frame = tk.Frame(self.root)
        bottom_frame.pack(side=tk.BOTTOM, fill=tk.X, pady=10)

        self.timer_label = tk.Label(bottom_frame, text=f"남은 시간: {self.timer}s", font=("Arial", 20))
        self.timer_label.pack(side=tk.LEFT, padx=10)

        self.result_label = tk.Label(
            bottom_frame,
            text="정확도: 0% | 분당 타자수: 0 | 맞춘 문제: 0 | MADE BY AJH",
            font=("Arial", 14)
        )
        self.result_label.pack(side=tk.LEFT, padx=10)

        self.check_button = tk.Button(bottom_frame, text="확인", command=self.check_results)
        self.check_button.pack(side=tk.RIGHT, padx=10)

        self.fetch_button = tk.Button(bottom_frame, text="새로운 문제 가져오기", command=self.load_files_from_github)
        self.fetch_button.pack(side=tk.RIGHT, padx=10)

        self.exit_button = tk.Button(bottom_frame, text="종료", command=self.exit_program)
        self.exit_button.pack(side=tk.RIGHT, padx=10)

        # Grid 레이아웃 확장 설정
        main_frame.columnconfigure(0, weight=1)  # 문제 칸 확장
        main_frame.columnconfigure(1, weight=1)  # 입력 칸 확장

    def load_cached_files(self):
        """assets 폴더에서 캐싱된 파일 로드."""
        for filename in os.listdir(self.assets_folder):
            filepath = os.path.join(self.assets_folder, filename)
            with open(filepath, "r", encoding="utf-8") as file:
                self.cached_files.append({
                    'name': filename,
                    'content': file.read()
                })

    def disable_screen_with_progress(self, message):
        """화면 비활성화 및 로딩 바 표시."""
        # 비활성화 창 생성
        self.overlay = tk.Toplevel(self.root)
        self.overlay.title("로딩 중")
        self.overlay.geometry("400x200")
        self.overlay.transient(self.root)
        self.overlay.grab_set()  # 화면 클릭 비활성화

        # 이벤트 무시 설정
        self.overlay.bind_all("<Button-1>", lambda e: "break")  # 마우스 클릭 무시
        self.overlay.bind_all("<Key>", lambda e: "break")  # 키보드 입력 무시

        # 메시지 표시
        label = tk.Label(self.overlay, text=message, font=("Arial", 14))
        label.pack(pady=10)

        # 로딩 바 생성
        self.progress_canvas = tk.Canvas(self.overlay, width=300, height=20, bg="white")
        self.progress_canvas.pack(pady=20)

        # 로딩 바 채우기 초기화
        self.progress_bar = self.progress_canvas.create_rectangle(0, 0, 0, 20, fill="blue")

        # 시작하자마자 절반 채우기
        self.update_progress_bar(50)

        # 0.5초 대기 후 나머지 로직 실행
        self.overlay.after(500, self.load_remaining_progress)

    def load_remaining_progress(self):
        """나머지 로딩 바를 채우는 작업."""
        # 이벤트 무시 설정
        self.overlay.bind_all("<Button-1>", lambda e: "break")  # 마우스 클릭 무시
        self.overlay.bind_all("<Key>", lambda e: "break")  # 키보드 입력 무시

        for i in range(51, 101):  # 50%에서 100%까지 증가
            self.update_progress_bar(i)
            time.sleep(0.02)  # 프로세스 진행을 느리게 보이도록 설정

        # 로딩이 완료되면 화면 활성화
        self.enable_screen()

    def update_progress_bar(self, progress_value):
        """로딩 바 업데이트."""
        # progress_value는 0 ~ 100 사이의 값
        bar_length = int((progress_value / 100) * 300)  # 300은 Canvas 너비
        self.progress_canvas.coords(self.progress_bar, 0, 0, bar_length, 20)  # 로딩 바 길이 업데이트
        self.progress_canvas.update_idletasks()  # 즉시 업데이트

    def enable_screen(self):
        """화면 클릭 활성화."""
        if hasattr(self, "overlay"):
            self.overlay.destroy()

    def load_files_from_github(self):
        """GitHub에서 파일을 5개 가져와 캐싱."""
        if os.path.exists(self.assets_folder):
            shutil.rmtree(self.assets_folder)  # 기존 폴더 삭제
        os.makedirs(self.assets_folder)  # 새 폴더 생성

        # 화면 비활성화 및 로딩 바 생성
        self.disable_screen_with_progress("GITHUB 오픈소스에서 문제를 로딩 중입니다...\n잠시만 기다려 주세요.(1분 정도 소요)")
        self.root.update()  # 화면 갱신

        # GitHub에서 파일 가져오기
        self.cached_files = self.fetch_github_files()[:5]  # 최대 5개 가져오기
        for idx, file_data in enumerate(self.cached_files, start=1):
            filename = file_data['name']
            content = file_data['content']
            with open(os.path.join(self.assets_folder, filename), "w", encoding="utf-8") as file:
                file.write(content)
            print(f"[LOG] {idx}. {filename} 가져오기 완료")  # 터미널 로그 출력

            # 로딩 바 업데이트
            progress_value = int((idx / len(self.cached_files)) * 100)
            self.update_progress_bar(progress_value)

        # 로딩 완료 메시지
        self.text_label.config(state=tk.NORMAL)
        self.text_label.delete("1.0", tk.END)
        self.text_label.insert(tk.END, "문제를 성공적으로 가져왔습니다!\n자동으로 시작합니다...")
        self.text_label.config(state=tk.DISABLED)

        # 화면 활성화
        self.enable_screen()

        # 2초 대기 후 문제 시작
        self.root.after(2000, self.next_problem)


    def fetch_github_files(self):
        """GitHub API에서 모든 파일 가져오기."""
        owner = "JELKOV"
        repo = "Deployfishshapedbread"
        token = os.getenv("GITHUB_TOKEN")
        base_url = f"https://api.github.com/repos/{owner}/{repo}/contents/"
        headers = {"Authorization": f"token {token}"}

        files = self.get_all_files(base_url, headers)
        file_data_list = []

        for file in files:
            file_url = file['download_url']
            response = requests.get(file_url, headers=headers)
            if response.status_code == 200:
                file_data_list.append({
                    'name': file['name'],
                    'content': response.text
                })
        return file_data_list

    def get_all_files(self, url, headers):
        """GitHub에서 모든 파일 재귀적으로 가져오기."""
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            items = response.json()
            all_files = []
            valid_extensions = ['.java']

            for item in items:
                if item['type'] == 'file' and any(item['name'].endswith(ext) for ext in valid_extensions):
                    all_files.append(item)
                elif item['type'] == 'dir':
                    dir_files = self.get_all_files(item['url'], headers)
                    all_files.extend(dir_files)
            return all_files
        else:
            return []

    def next_problem(self):
        """다음 문제를 로드하고 입력창 초기화."""
        if self.problem_count >= self.max_problems:
            self.show_final_result()
            self.exit_program()
            return

        self.timer = self.total_time  # 타이머 리셋
        self.start_time = time.time()  # 시작 시간 기록
        self.update_timer()  # 타이머 시작

        self.sample_text = random.choice(self.cached_files)['content']
        self.text_label.config(state=tk.NORMAL)
        self.text_label.delete("1.0", tk.END)
        self.text_label.insert(tk.END, self.sample_text)
        self.text_label.config(state=tk.DISABLED)

        self.entry.delete("1.0", tk.END)
        self.result_label.config(
            text=f"정확도: 0.00% | 분당 타자수: 0 | 맞춘 문제: {self.correct_problems} | MADE BY AJH"
        )

    def update_timer(self):
        """타이머 업데이트."""
        if self.timer > 0:
            self.timer -= 1
            self.timer_label.config(text=f"남은 시간: {self.timer}s")
            self.root.after(1000, self.update_timer)
        else:
            self.check_results()

    def check_typing(self, event=None):
        """실시간 WPM 및 정확도 계산."""
        typed_text = self.entry.get("1.0", tk.END).strip()
        expected_text = self.sample_text.strip()

        # 정확도 계산 (공백 무시)
        correct_chars = sum(1 for t, e in zip(typed_text, expected_text) if t == e or t == " " or e == " ")
        accuracy = (correct_chars / len(expected_text)) * 100 if expected_text else 0

        # 분당 타자수 계산
        elapsed_time = max(1, time.time() - self.start_time)  # 최소 1초로 설정
        words_typed = len(typed_text.split())
        wpm = int(words_typed / (elapsed_time / 60)) if words_typed > 0 else 0

        # 틀린 부분 하이라이트
        self.highlight_errors(typed_text, expected_text)

        # 결과 업데이트
        self.result_label.config(
            text=f"정확도: {accuracy:.2f}% | 분당 타자수: {wpm} | 맞춘 문제: {self.correct_problems} | MADE BY AJH"
        )

    def highlight_errors(self, typed_text, expected_text):
        """입력한 텍스트에서 틀린 부분을 하이라이트."""
        self.entry.tag_remove("error", "1.0", tk.END)  # 기존 하이라이트 제거
        expected_lines = expected_text.splitlines()
        typed_lines = typed_text.splitlines()

        for line_idx, (typed_line, expected_line) in enumerate(zip(typed_lines, expected_lines)):
            for char_idx, (typed_char, expected_char) in enumerate(zip(typed_line, expected_line)):
                if typed_char != expected_char:  # 틀린 문자만 처리
                    position_start = f"{line_idx + 1}.{char_idx}"
                    position_end = f"{line_idx + 1}.{char_idx + 1}"
                    self.entry.tag_add("error", position_start, position_end)

        # 하이라이트 스타일 정의
        self.entry.tag_config("error", background="yellow", foreground="red")

    def check_results(self):
        """Check 버튼 클릭 시 결과 확인."""
        typed_text = self.entry.get("1.0", tk.END).strip()
        expected_text = self.sample_text.strip()

        correct_chars = sum(1 for t, e in zip(typed_text, expected_text) if t == e or t == " " or e == " ")
        accuracy = (correct_chars / len(expected_text)) * 100 if expected_text else 0

        if accuracy == 100.0:
            self.correct_problems += 1
            self.problem_count += 1
            messagebox.showinfo("정답", "모두 맞췄습니다! 다음 문제로 이동합니다.")
            self.next_problem()
        else:
            self.highlight_errors(typed_text, expected_text)
            messagebox.showerror("오답", f"정확도: {accuracy:.2f}%. 다시 시도하세요.")

    def show_final_result(self):
        """최종 결과 표시."""
        messagebox.showinfo("결과", f"총 맞춘 문제 수: {self.correct_problems} / {self.max_problems}")

    def exit_program(self):
        """프로그램 종료."""
        self.root.destroy()

    def exit_fullscreen(self, event=None):
        """전체 화면 종료."""
        self.root.attributes("-fullscreen", False)

if __name__ == "__main__":
    root = tk.Tk()
    app = TypingSpeedApp(root)
    root.mainloop()
