import yaml from "yaml";
import {watch, WatchOptions} from "chokidar";
import * as path from "node:path";
import * as fs from "node:fs";

interface SyncConfig {
    [key: string]: {
        base: string
        source: string[]
        target: string[]
        files: string[]
    }
}

const configPath = Bun.env.SYNC_CONFIG || "config/sync.yml"
const configRaw = await Bun.file(configPath).text()
const config: SyncConfig = yaml.parse(configRaw);

const option: WatchOptions = {
    persistent: true,
    ignoreInitial: true,
    usePolling: true
}

watch(configPath, option).on("all" , (event, filename) => {
    if(event !== "unlink") {
        process.exit(1);
    }
})

console.log("Sync - 1.0.3")
console.log("- usePolling")

const app = () => {

    Object.keys(config).forEach(key => {
        const item = config[key];

        item.files.forEach(file => {
            item.source.forEach((sourceId) => {
                const source = item.base
                    .replace("{id}", sourceId)
                    .replace("{file}", file)

                item.target.forEach((targetId) => {

                    const target = item.base
                        .replace("{id}", targetId)
                        .replace("{file}", file)

                    try {
                        fs.rmSync(target, {recursive: true});
                        copyRecursive(source, target);
                    } catch (e) {

                    }

                    console.log(`Initial sync completed: ${source} -> ${target}`);

                    const sourceWatcher = watch(source, option);
                    const targetWatcher = watch(target, option);

                    sourceWatcher.on('all', (event, fileChange) => {
                        try {
                            const relativePath = path.relative(source, fileChange);
                            const targetPath = path.join(target, relativePath);

                            if (event === "unlink" || event === "unlinkDir") {
                                fs.rmSync(targetPath, {recursive: true, force: true});
                                console.log(`Deleted in target: ${targetPath}`);
                            } else {
                                copyRecursive(fileChange, targetPath);
                                console.log(`Synced from source to target: ${fileChange} -> ${targetPath}`);
                            }
                        } catch (e) {

                        }
                    });

                    if (Bun.env.USE_TARGET === "true") {

                        targetWatcher.on('all', (event, fileChange) => {
                            try {
                                const relativePath = path.relative(target, fileChange);
                                const sourcePath = path.join(source, relativePath);

                                if (event === "unlink" || event === "unlinkDir") {
                                    fs.rmSync(sourcePath, {recursive: true, force: true});
                                    console.log(`Deleted in source: ${sourcePath}`);
                                } else {
                                    copyRecursive(fileChange, sourcePath);
                                    console.log(`Synced from target to source: ${fileChange} -> ${sourcePath}`);
                                }
                            } catch (e) {

                            }
                        });

                        console.log(`Watching: ${source} <-> ${target}`);
                        return;
                    }
                    console.log(`Watching: ${source} -> ${target}`);
                })

            })
        })

    })

}

app()

function copyRecursive(source: string, target: string) {
    fs.cpSync(source, target, {recursive: true});
    changePermissions(target);
}

function changePermissions(targetPath: string) {
    fs.chmodSync(targetPath, 0o777); // เปลี่ยนสิทธิ์ให้เป็น 777

    // ถ้า targetPath เป็นโฟลเดอร์, ให้เปลี่ยนสิทธิ์ทุกไฟล์และโฟลเดอร์ภายในด้วย
    if (fs.statSync(targetPath).isDirectory()) {
        fs.readdirSync(targetPath).forEach(file => {
            const fullPath = path.join(targetPath, file);
            changePermissions(fullPath); // เรียกใช้ฟังก์ชันสำหรับเปลี่ยนสิทธิ์ไฟล์หรือโฟลเดอร์ภายใน
        });
    }
}