import * as fs from "node:fs";
import JSZip from "jszip";

const MINECRAFT_VERSION = "1.20.1"

const REGIONS = await Bun.file("settings.json").json()

interface Dimension {
    type: string
    generator: {
        type: string
        biome_source: {
            biomes: Biome[]
        }
    }
}

interface Biome {
    biome: string
    parameters: any
}

const splitBiomes = (defaultDimension: Dimension, LIST_BIOMES: string[]) => {

    const dimension = structuredClone(defaultDimension);

    dimension.generator.biome_source.biomes = defaultDimension.generator.biome_source.biomes.filter((item: any) => {
        return LIST_BIOMES.map(item => item.toLowerCase()).includes(item.biome.toLowerCase().replace('minecraft:', ''));
    })

    return dimension;
}

// const getDefaultDimension = async (version: string) => {
//     const url = `https://raw.githubusercontent.com/misode/mcmeta/${version}-data/data/minecraft/dimension/overworld.json`
//     const response = await fetch(url)
//
//     return await response.json() as Dimension
// }

const getDefaultDimension = (version: string) => {
    return Bun.file("overworld.json").json()
}

const defaultDimension = await getDefaultDimension(MINECRAFT_VERSION)

const defaultZip = fs.readFileSync("default.zip")

const zip = new JSZip()
await zip.loadAsync(defaultZip).then(async (content) => {
    for (const region of REGIONS) {
        const dimension = splitBiomes(defaultDimension, region.biomes)
        zip.file(`data/manow/dimension/${region.name}.json`, JSON.stringify(dimension));
    }

    const zipBuffer = await zip.generateAsync({type: "nodebuffer"})
    fs.writeFileSync("split-biome.zip", zipBuffer)
    console.log("Done =>", "split-biome.zip")
})

console.log("Exiting in 5s")
await Bun.sleep(5000)