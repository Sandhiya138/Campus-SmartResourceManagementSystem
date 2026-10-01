import { useState } from "react";
import api, { errMsg } from "./api";

export default function Login({ onLogin, goRegister }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const submit = async () => {
    try {
      const res = await api.post("/auth/login", { username, password });
      localStorage.setItem("token", res.data.token);
      const user = { username: res.data.username, role: res.data.role };
      localStorage.setItem("user", JSON.stringify(user));
      onLogin(user);
    } catch (e) {
      setError(errMsg(e));
    }
  };

  return (
    <div>
      <h2>Login</h2>
      <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} /><br />
      <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} /><br />
      <button onClick={submit}>Login</button>
      <p className="err">{error}</p>
      <p>No account? <button onClick={goRegister}>Register</button></p>
    </div>
  );
}
