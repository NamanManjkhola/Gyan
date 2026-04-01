import { ChangeEvent, FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import {
  askQuestion,
  ChatRecord,
  deleteChat,
  deleteDocument,
  DocumentRecord,
  downloadDocument,
  getChat,
  getChatDocuments,
  logout,
  uploadDocument
} from '../lib/api';

function renderInlineFormatting(text: string) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g).filter(Boolean);

  return parts.map((part, index) => {
    const boldMatch = part.match(/^\*\*([^*]+)\*\*$/);

    if (boldMatch) {
      return (
        <strong key={`strong-${index}`} className="answer-strong">
          {boldMatch[1]}
        </strong>
      );
    }

    return <span key={`text-${index}`}>{part}</span>;
  });
}

function renderAnswer(answer: string) {
  const blocks = answer
    .split(/\n\s*\n/)
    .map((block) => block.trim())
    .filter(Boolean);

  return blocks.map((block, index) => {
    const lines = block
      .split('\n')
      .map((line) => line.trim())
      .filter(Boolean);

    const isBulletList = lines.every((line) => /^[-*]\s+/.test(line));
    const isNumberedList = lines.every((line) => /^\d+\.\s+/.test(line));

    if (isBulletList) {
      return (
        <ul className="answer-list" key={`bullet-${index}`}>
          {lines.map((line, lineIndex) => (
            <li key={`bullet-item-${index}-${lineIndex}`}>
              {renderInlineFormatting(line.replace(/^[-*]\s+/, ''))}
            </li>
          ))}
        </ul>
      );
    }

    if (isNumberedList) {
      return (
        <ol className="answer-list" key={`number-${index}`}>
          {lines.map((line, lineIndex) => (
            <li key={`number-item-${index}-${lineIndex}`}>
              {renderInlineFormatting(line.replace(/^\d+\.\s+/, ''))}
            </li>
          ))}
        </ol>
      );
    }

    return (
      <p className="answer-paragraph" key={`paragraph-${index}`}>
        {renderInlineFormatting(lines.join(' '))}
      </p>
    );
  });
}

export function ChatPage() {
  const navigate = useNavigate();
  const params = useParams();
  const chatId = Number(params['chatId']);
  const [chat, setChat] = useState<ChatRecord | null>(null);
  const [documents, setDocuments] = useState<DocumentRecord[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [loadingChat, setLoadingChat] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [asking, setAsking] = useState(false);
  const [viewingId, setViewingId] = useState<number | null>(null);
  const [deletingDocumentId, setDeletingDocumentId] = useState<number | null>(null);
  const [deletingChat, setDeletingChat] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (!Number.isFinite(chatId)) {
      navigate('/dashboard', { replace: true });
      return;
    }

    async function loadWorkspace() {
      setLoadingChat(true);
      setErrorMessage('');

      try {
        const [chatResponse, documentsResponse] = await Promise.all([
          getChat(chatId),
          getChatDocuments(chatId)
        ]);
        setChat(chatResponse);
        setDocuments(documentsResponse);
      } catch (err) {
        setErrorMessage(err instanceof Error ? err.message : 'Unable to load chat.');
      } finally {
        setLoadingChat(false);
      }
    }

    void loadWorkspace();
  }, [chatId, navigate]);

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    setSelectedFile(event.target.files?.[0] ?? null);
    setStatusMessage('');
  }

  async function refreshDocuments() {
    const response = await getChatDocuments(chatId);
    setDocuments(response);
  }

  async function handleUpload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!selectedFile) {
      setErrorMessage('Select a file before uploading.');
      return;
    }

    setUploading(true);
    setErrorMessage('');
    setStatusMessage('');

    try {
      await uploadDocument(chatId, selectedFile);
      setSelectedFile(null);
      setStatusMessage('Document uploaded successfully.');
      await refreshDocuments();
      setChat((current) =>
        current
          ? { ...current, documentCount: current.documentCount + 1, updatedAt: new Date().toISOString() }
          : current
      );
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Upload failed.');
    } finally {
      setUploading(false);
    }
  }

  async function handleAsk(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!question.trim()) {
      setErrorMessage('Enter a question first.');
      return;
    }

    setAsking(true);
    setErrorMessage('');

    try {
      const response = await askQuestion(chatId, question.trim());
      setAnswer(response.answer);
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Unable to get an answer.');
    } finally {
      setAsking(false);
    }
  }

  async function handleDownload(documentId: number) {
    setViewingId(documentId);
    setErrorMessage('');
    setStatusMessage('');

    try {
      await downloadDocument(chatId, documentId);
      setStatusMessage('Download started.');
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Unable to download document.');
    } finally {
      setViewingId(null);
    }
  }

  async function handleDeleteDocument(document: DocumentRecord) {
    const confirmed = window.confirm(`Delete "${document.fileName}" from this chat?`);

    if (!confirmed) {
      return;
    }

    setDeletingDocumentId(document.id);
    setErrorMessage('');
    setStatusMessage('');

    try {
      await deleteDocument(chatId, document.id);
      setDocuments((current) => current.filter((item) => item.id !== document.id));
      setChat((current) =>
        current
          ? {
              ...current,
              documentCount: Math.max(0, current.documentCount - 1),
              updatedAt: new Date().toISOString()
            }
          : current
      );
      setStatusMessage('Document deleted successfully.');
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Unable to delete document.');
    } finally {
      setDeletingDocumentId(null);
    }
  }

  async function handleDeleteChat() {
    if (!chat) {
      return;
    }

    const confirmed = window.confirm(`Delete chat "${chat.name}" and all its documents?`);

    if (!confirmed) {
      return;
    }

    setDeletingChat(true);
    setErrorMessage('');
    setStatusMessage('');

    try {
      await deleteChat(chat.id);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Unable to delete chat.');
      setDeletingChat(false);
    }
  }

  async function handleLogout() {
    await logout();
    window.location.assign('/');
  }

  function formatFileSize(size: number) {
    if (size < 1024) {
      return `${size} B`;
    }

    if (size < 1024 * 1024) {
      return `${(size / 1024).toFixed(1)} KB`;
    }

    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

  if (!Number.isFinite(chatId)) {
    return null;
  }

  return (
    <main className="dashboard-shell">
      <header className="hero">
        <div>
          <p className="eyebrow">Document Chat</p>
          <h1>{chat?.name ?? 'Loading chat...'}</h1>
          <p className="lede">
            Upload documents and ask questions inside this dedicated workspace.
          </p>
        </div>

        <div className="hero-actions">
          <Link className="ghost-button" to="/dashboard">
            Back to documents
          </Link>
          <button className="ghost-button danger-button" type="button" onClick={handleDeleteChat} disabled={deletingChat}>
            {deletingChat ? 'Deleting chat...' : 'Delete chat'}
          </button>
          <button className="ghost-button" type="button" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      {errorMessage ? (
        <section className="status-strip">
          <p className="status error">{errorMessage}</p>
        </section>
      ) : null}

      {statusMessage ? (
        <section className="status-strip">
          <p className="status success">{statusMessage}</p>
        </section>
      ) : null}

      <section className="dashboard-grid dashboard-chat-grid">
        <form className="panel" onSubmit={handleUpload}>
          <div className="panel-header">
            <div>
              <p className="card-kicker">Upload</p>
              <h2>Add documents to this chat</h2>
            </div>
          </div>

          <label className="upload-dropzone">
            <input type="file" onChange={handleFileChange} />
            <span>{selectedFile ? selectedFile.name : 'Choose a file to upload'}</span>
          </label>

          <button className="primary-button" type="submit" disabled={uploading || loadingChat}>
            {uploading ? 'Uploading...' : 'Upload document'}
          </button>
        </form>

        <form className="panel chat-panel" onSubmit={handleAsk}>
          <div className="panel-header">
            <div>
              <p className="card-kicker">Chat</p>
              <h2>Ask about this chat's documents</h2>
            </div>
          </div>

          <label className="field">
            <span>Question</span>
            <textarea
              rows={6}
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="Ask something about the uploaded documents"
            />
          </label>

          <button className="primary-button secondary-accent" type="submit" disabled={asking || loadingChat}>
            {asking ? 'Thinking...' : 'Ask'}
          </button>

          {answer ? (
            <section className="answer-card">
              <p className="card-kicker">Answer</p>
              <div className="answer-content">{renderAnswer(answer)}</div>
            </section>
          ) : null}
        </form>
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="card-kicker">Documents</p>
            <h2>Files in this chat</h2>
          </div>
          <button className="ghost-button" type="button" onClick={() => void refreshDocuments()}>
            Refresh
          </button>
        </div>

        {loadingChat ? <p className="empty-state">Loading chat workspace...</p> : null}

        {!loadingChat && documents.length === 0 ? (
          <p className="empty-state">No documents uploaded to this chat yet.</p>
        ) : null}

        {!loadingChat && documents.length > 0 ? (
          <div className="document-list">
            {documents.map((document) => (
              <article className="document-card" key={document.id}>
                <div>
                  <h3>{document.fileName}</h3>
                  <p>{formatFileSize(document.fileSize)}</p>
                </div>

                <div className="row-actions">
                  <button
                    className="ghost-button"
                    type="button"
                    onClick={() => void handleDownload(document.id)}
                    disabled={viewingId === document.id || deletingDocumentId === document.id}
                  >
                    {viewingId === document.id ? 'Downloading...' : 'Download'}
                  </button>
                  <button
                    className="ghost-button danger-button"
                    type="button"
                    onClick={() => void handleDeleteDocument(document)}
                    disabled={deletingDocumentId === document.id || viewingId === document.id}
                  >
                    {deletingDocumentId === document.id ? 'Deleting...' : 'Delete'}
                  </button>
                </div>
              </article>
            ))}
          </div>
        ) : null}
      </section>
    </main>
  );
}
