import { useState } from "react";
import ChatWindow from "../components/chat/ChatWindow";
import UploadBox from "../components/Upload/UploadBox";
import DocumentList from "../components/Upload/DocumentList";
import { useAuthStore } from "../auth/authStore";

export default function Dashboard() {
  const logout = useAuthStore((s) => s.logout);
  const [refresh, setRefresh] = useState(0);

  return (
    <div className="h-screen flex flex-col">

      {/* Header */}
      <div className="flex justify-between p-4 bg-gray-800 text-white">
        <h1>AI Docs</h1>
        <button onClick={logout}>Logout</button>
      </div>

      {/* Main Layout */}
      <div className="flex flex-1">

        {/* Left Panel */}
        <div className="w-1/3 p-4 border-r overflow-y-auto">
          <UploadBox onUploadSuccess={() => setRefresh((r) => r + 1)} />
          <DocumentList key={refresh} />
        </div>

        {/* Right Panel */}
        <div className="flex-1 p-4">
          <ChatWindow />
        </div>

      </div>

    </div>
  );
}