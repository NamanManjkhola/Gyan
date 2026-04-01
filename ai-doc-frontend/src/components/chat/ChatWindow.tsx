// src/components/Chat/ChatWindow.tsx
import { useState } from "react";
import api from "../../api/axios";

export default function ChatWindow() {
  const [messages, setMessages] = useState<any[]>([]);
  const [input, setInput] = useState("");

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userMsg = { role: "user", content: input };
    setMessages((prev) => [...prev, userMsg]);

    const question = input;
    setInput("");

    const res = await api.post("/ai/ask", { question });

    const aiMsg = {
      role: "ai",
      content: res.data.answer,
      sources: res.data.sources,
    };

    setMessages((prev) => [...prev, aiMsg]);
  };

  return (
    <div className="flex flex-col h-[80vh]">

      <div className="flex-1 overflow-y-auto p-4">
        {messages.map((msg, i) => (
          <div
            key={i}
            className={`mb-2 p-2 rounded ${
              msg.role === "user"
                ? "bg-blue-500 text-white text-right"
                : "bg-gray-200"
            }`}
          >
            {msg.content}

            {/* Sources */}
            {msg.sources && (
              <div className="text-xs mt-2">
                {msg.sources.map((s: any, idx: number) => (
                  <div key={idx}>
                    📄 {s.docName} (p{s.page})
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>

      <div className="flex p-2 border-t">
        <input
          className="flex-1 border p-2"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask something..."
        />
        <button
          onClick={sendMessage}
          className="ml-2 bg-blue-500 text-white px-4"
        >
          Send
        </button>
      </div>

    </div>
  );
}