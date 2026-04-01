import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ChatRecord, createChat, deleteChat, getChats, logout } from '../lib/api';

export function DashboardPage() {
  const navigate = useNavigate();
  const [chats, setChats] = useState<ChatRecord[]>([]);
  const [chatName, setChatName] = useState('');
  const [loadingChats, setLoadingChats] = useState(true);
  const [creatingChat, setCreatingChat] = useState(false);
  const [deletingChatId, setDeletingChatId] = useState<number | null>(null);
  const [statusMessage, setStatusMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  async function loadChats() {
    setLoadingChats(true);
    setErrorMessage('');

    try {
      const response = await getChats();
      setChats(response);
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Unable to load chats.');
    } finally {
      setLoadingChats(false);
    }
  }

  useEffect(() => {
    void loadChats();
  }, []);

  async function handleCreateChat(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!chatName.trim()) {
      setErrorMessage('Enter a chat name first.');
      return;
    }

    setCreatingChat(true);
    setErrorMessage('');
    setStatusMessage('');

    try {
      const chat = await createChat(chatName.trim());
      setStatusMessage('Chat created successfully.');
      setChatName('');
      await loadChats();
      navigate(`/chat/${chat.id}`);
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Unable to create chat.');
    } finally {
      setCreatingChat(false);
    }
  }

  async function handleLogout() {
    await logout();
    window.location.assign('/');
  }

  async function handleDeleteChat(chat: ChatRecord) {
    const confirmed = window.confirm(`Delete chat "${chat.name}" and all its documents?`);

    if (!confirmed) {
      return;
    }

    setDeletingChatId(chat.id);
    setErrorMessage('');
    setStatusMessage('');

    try {
      await deleteChat(chat.id);
      setChats((current) => current.filter((item) => item.id !== chat.id));
      setStatusMessage('Chat deleted successfully.');
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Unable to delete chat.');
    } finally {
      setDeletingChatId(null);
    }
  }

  function formatDate(value: string) {
    return new Date(value).toLocaleString();
  }

  return (
    <main className="dashboard-shell">
      <header className="hero">
        <div>
          <p className="eyebrow">Chat Workspace</p>
          <h1>Your chats</h1>
          <p className="lede">
            Create up to five focused workspaces, each with its own documents and chat context.
          </p>
        </div>

        <div className="hero-actions">
          <button className="ghost-button" type="button" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <section className="dashboard-grid">
        <form className="panel" onSubmit={handleCreateChat}>
          <div className="panel-header">
            <div>
              <p className="card-kicker">New Chat</p>
              <h2>Create a workspace</h2>
            </div>
          </div>

          <label className="field">
            <span>Chat name</span>
            <input
              type="text"
              value={chatName}
              onChange={(event) => setChatName(event.target.value)}
              placeholder="Quarterly invoices"
              maxLength={80}
            />
          </label>

          <button className="primary-button" type="submit" disabled={creatingChat || chats.length >= 5}>
            {creatingChat ? 'Creating...' : chats.length >= 5 ? 'Chat limit reached' : 'Create chat'}
          </button>

          <p className="empty-state">You can create up to 5 chats per account.</p>
        </form>
      </section>

      {(statusMessage || errorMessage) && (
        <section className="status-strip">
          {statusMessage ? <p className="status success">{statusMessage}</p> : null}
          {errorMessage ? <p className="status error">{errorMessage}</p> : null}
        </section>
      )}

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="card-kicker">Chat List</p>
            <h2>Your workspaces</h2>
          </div>
          <button className="ghost-button" type="button" onClick={() => void loadChats()}>
            Refresh
          </button>
        </div>

        {loadingChats ? <p className="empty-state">Loading chats...</p> : null}

        {!loadingChats && chats.length === 0 ? (
          <p className="empty-state">No chats created yet.</p>
        ) : null}

        {!loadingChats && chats.length > 0 ? (
          <div className="document-list">
            {chats.map((chat) => (
              <article className="document-card" key={chat.id}>
                <div>
                  <h3>{chat.name}</h3>
                  <p>
                    {chat.documentCount} {chat.documentCount === 1 ? 'document' : 'documents'} · Updated{' '}
                    {formatDate(chat.updatedAt)}
                  </p>
                </div>

                <div className="row-actions">
                  <Link className="ghost-button" to={`/chat/${chat.id}`}>
                    Open chat
                  </Link>
                  <button
                    className="ghost-button danger-button"
                    type="button"
                    onClick={() => void handleDeleteChat(chat)}
                    disabled={deletingChatId === chat.id}
                  >
                    {deletingChatId === chat.id ? 'Deleting...' : 'Delete'}
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
