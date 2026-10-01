import { useState } from "react";
import api, { errMsg } from "./api";

export default function Register({ goLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("STUDENT");
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  const submit = async () => {
    setMsg("");
    setError("");
    try {
      await api.post("/auth/register", { username, password, role });
      setMsg("Registered! Wait for admin approval, then login.");
    } catch (e) {
      setError(errMsg(e));
    }
  };

  return (
    <div>
      <h2>Register</h2>
      <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} /><br />
      <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} /><br />
      <select value={role} onChange={(e) => setRole(e.target.value)}>
        <option>STUDENT</option>
        <option>FACULTY</option>
      </select><br />
      <button onClick={submit}>Register</button>
      {msg && <p className="msg">{msg}</p>}
      <p className="err">{error}</p>
      <p>Already have an account? <button onClick={goLogin}>Login</button></p>
    </div>
  );
}
