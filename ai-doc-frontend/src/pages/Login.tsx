import { useState } from "react";
import api from "../api/axios";
import { useAuthStore } from "../auth/authStore";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const setToken = useAuthStore((s) => s.setToken);
  const navigate = useNavigate();

  const login = async () => {
    const res = await api.post("/auth/login", { email, password });
    setToken(res.data.token);
    navigate("/");
  };

  return (
    <div className="flex h-screen items-center justify-center">

      <div className="p-6 shadow-lg rounded-xl w-80">

        <h2 className="text-xl mb-4">Login</h2>

        <input
          className="border p-2 w-full mb-3"
          placeholder="Email"
          onChange={(e) => setEmail(e.target.value)}
        />

        <input
          type="password"
          className="border p-2 w-full mb-3"
          placeholder="Password"
          onChange={(e) => setPassword(e.target.value)}
        />

        <button
          onClick={login}
          className="bg-blue-500 text-white p-2 w-full rounded"
        >
          Login
        </button>

      </div>

    </div>
  );
}