import { useEffect, useState } from "react";
import api from "../../api/axios";

export default function DocumentList() {
  const [docs, setDocs] = useState<any[]>([]);

  const fetchDocs = async () => {
    const res = await api.get("/documents");
    setDocs(res.data);
  };

  useEffect(() => {
    fetchDocs();
  }, []);

  return (
    <div className="border p-4 rounded-lg shadow-md">

      <h2 className="mb-2 font-semibold">Your Documents</h2>

      {docs.length === 0 && <p>No documents uploaded</p>}

      {docs.map((doc) => (
        <div
          key={doc.id}
          className="flex justify-between p-2 border-b"
        >
          <span>{doc.name}</span>

          <a
            href={`http://localhost:8080/documents/${doc.id}`}
            target="_blank"
            className="text-blue-500"
          >
            Download
          </a>
        </div>
      ))}

    </div>
  );
}