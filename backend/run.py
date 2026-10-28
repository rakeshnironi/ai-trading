import os
import sys

root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
site_packages = os.path.join(root_dir, "backend", ".venv", "lib", "python3.11", "site-packages")
if os.path.exists(site_packages) and site_packages not in sys.path:
    sys.path.insert(0, site_packages)
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

import uvicorn

if __name__ == "__main__":
    host = os.getenv("HOST", "0.0.0.0")
    # Use APP_PORT or port 3000 (nginx forwards / to port 3000)
    port = int(os.getenv("APP_PORT", "3000"))
    print(f"Starting Trading System Backend Server on http://{host}:{port}")
    uvicorn.run("backend.app.main:app", host=host, port=port, reload=False, ws="none")
